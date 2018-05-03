import com.alibaba.fastjson.JSON;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.*;
import model.ImageAndroid;
import model.ReleaseGoogleLang;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class BackUpApkInfo {


    
    public Integer releaseId;

    public String appName;

    public String packageName;

    //保存在当前线程中
    public  ThreadLocal<AndroidPublisher.Edits> editsThreadLocal = new ThreadLocal<>() ;
    public  ThreadLocal<String> editIdThreadLocal = new ThreadLocal<>() ;

    //应用的Alpha和Beta版本部署到您分配给Alpha和Beta测试组的用户F
    public static final String TRACK_ALPHA = "alpha";
    public static final String TRACK_BETA = "beta";
    public static final String TRACK_PROD = "production";
    public static final String TRACK_ROLLOUT = "rollout";//“部署”版本（“分阶段发布”的简称）


    public static final String IMAGE_JPG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";

    public static final String IMAGETYPE_FETUREGRAPIC = "featureGraphic";//置顶大图
    public static final String IMAGETYPE_ICON = "icon";//图标
    public static final String IMAGETYPE_SCREENSHOTS = "phoneScreenshots";//手机展示截图


    public BackUpApkInfo() {
    }

    public BackUpApkInfo(Integer releaseId, String appName, String packageName) {
        this.releaseId = releaseId;
        this.appName = appName;
        this.packageName = packageName;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(Integer releaseId) {
        this.releaseId = releaseId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    

    private  void initEditsThreadLocal(String applicatinName ,String packageName)
            throws IOException, GeneralSecurityException {
        AndroidPublisher.Edits edits = editsThreadLocal.get();
        String editId = editIdThreadLocal.get();
        if(edits == null ||editId == null) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(packageName),
                    "packageName cannot be null or empty!");

            // Create the API service.
            AndroidPublisher service = AndroidPublisherHelper.init(
                    applicatinName, ApplicationConfig.SERVICE_ACCOUNT_EMAIL);

            edits = service.edits();
            editId = edits.insert(packageName, null /** no content */) .execute().getId();
            System.out.println(String.format("Created edit with id: %s", editId));
            editsThreadLocal.set(edits);
            editIdThreadLocal.set(editId);
        }
        
    }


    /**
     * 实际google play提交操作
     * @param packageName
     * @throws IOException
     */
    private void commitEdits(String packageName) throws IOException {
        AndroidPublisher.Edits edits = editsThreadLocal.get();
        String editId = editIdThreadLocal.get();
        // Commit changes for edit.
        AndroidPublisher.Edits.Commit commitRequest = edits.commit(packageName, editId);
        AppEdit appEdit = commitRequest.execute();
        System.out.println(String.format("App edit with id %s has been comitted", appEdit.getId()));
        //每次提交完毕  需要置空  防止commit后继续执行失败
        editsThreadLocal.set(null);
        editIdThreadLocal.set(null);
        System.out.println("***google play 提交Edits 完成！！！");
    }


    public Map getApkBackupLists(Integer releaseId, String applicatinName , String packageName ){
        {
            Map resultMap = new HashMap();
            List<ReleaseGoogleLang> langList = new ArrayList<>();
            List<ImageAndroid> imageList = new ArrayList<>();
            resultMap.put("langList",langList);
            resultMap.put("imageList",imageList);
            try {
                initEditsThreadLocal(applicatinName,packageName);

                AndroidPublisher.Edits edits = editsThreadLocal.get();
                String editId = editIdThreadLocal.get();

                // Get a list of apks.获取
                ApksListResponse apksResponse = edits.apks()
                        .list(packageName, editId)
                        .execute();
                // Print the apk info.

                int apkOldVersion = 0;
                for (Apk apk : apksResponse.getApks()) {
                    System.out.println(JSON.toJSONString(apk));
                    //获取较大值versionCode
                    apkOldVersion = apk.getVersionCode()>apkOldVersion ?apk.getVersionCode():apkOldVersion;
                }
                //获取每种语言 对应的文案信息  拉取到本地
                ListingsListResponse listingsListResponse = edits.listings().list(packageName,editId).execute();
                HashMap<String,ReleaseGoogleLang> hashMap = new HashMap<>();
                for(Listing list: listingsListResponse.getListings()){
                    ReleaseGoogleLang lang =  new ReleaseGoogleLang();
                    lang.setReleaseId(0-releaseId);
                    lang.setLanguage(list.getLanguage());
                    lang.setTitle(list.getTitle());
                    lang.setShortDesc(list.getShortDescription());
                    lang.setFullDesc(list.getFullDescription());
                    lang.setVideoUrl(list.getVideo());
                    hashMap.put(list.getLanguage(),lang);
                    langList.add(lang);
                    System.out.println(JSON.toJSONString(list));
                }

                //获取apk  每种语言最近更新内容的远程备份
                ApkListingsListResponse apkListingsListResponse = edits.apklistings()
                        .list(packageName,editId,apkOldVersion ).execute();

                if (apkListingsListResponse.getListings() !=null ) {
                    for(ApkListing apkListing:apkListingsListResponse.getListings()){
                        ReleaseGoogleLang lang = hashMap.get(apkListing.getLanguage());
                        if(lang == null){
                            lang= new ReleaseGoogleLang();
                            lang.setReleaseId(0-releaseId);
                            lang.setLanguage(apkListing.getLanguage());
                            langList.add(lang);
                        }
                        lang.setRecentChanges(apkListing.getRecentChanges());
                        System.out.println(JSON.toJSONString(apkListing));
                    }
                }

                for(Map.Entry<String,ReleaseGoogleLang> entry :hashMap.entrySet()) {
                    ImagesListResponse imagesListResponse =
                            edits.images().list(packageName,
                                    editId, entry.getKey(), IMAGETYPE_SCREENSHOTS)
                                    .execute();
                    int x= 0;
                    if(imagesListResponse.getImages() !=null) {
                        for (Image image : imagesListResponse.getImages()) {
                            ImageAndroid imageAndroid = new ImageAndroid();
                            imageAndroid.setReleaseId(0 - releaseId);
                            imageAndroid.setLanguage(entry.getKey());
                            //google  play保存的图片的json信息 包括图片id  图片url
                            imageAndroid.setImageReturnUrl(JSON.toJSONString(image));
                            imageAndroid.setImageUse(IMAGETYPE_SCREENSHOTS);
                            imageAndroid.setOrdinal(x + 1);
                            x++;
                            imageList.add(imageAndroid);
                        }
                    }
                    ImagesListResponse imagesListResponse2 =
                            edits.images().list(packageName,
                                    editId, entry.getKey(), IMAGETYPE_FETUREGRAPIC)
                                    .execute();
                    if(imagesListResponse2.getImages() !=null) {
                        for (Image image : imagesListResponse2.getImages()) {
                            ImageAndroid imageAndroid = new ImageAndroid();
                            imageAndroid.setReleaseId(0 - releaseId);
                            imageAndroid.setLanguage(entry.getKey());
                            imageAndroid.setImageReturnUrl(image.getUrl());
                            imageAndroid.setImageUse(IMAGETYPE_FETUREGRAPIC);
                            imageList.add(imageAndroid);
                        }
                    }

                    ImagesListResponse imagesListResponse3 =
                            edits.images().list(packageName,
                                    editId, entry.getKey(), IMAGETYPE_ICON)
                                    .execute();
                    if(imagesListResponse3.getImages() !=null) {
                        for (Image image : imagesListResponse3.getImages()) {
                            ImageAndroid imageAndroid = new ImageAndroid();
                            imageAndroid.setReleaseId(0 - releaseId);
                            imageAndroid.setLanguage(entry.getKey());
                            imageAndroid.setImageReturnUrl(image.getUrl());
                            imageAndroid.setImageUse(IMAGETYPE_ICON);
                            imageList.add(imageAndroid);
                        }
                    }
                }
            } catch (IOException  | GeneralSecurityException ex) {
                System.out.println("Excpetion was thrown while backUp  apkListings track"+ex);
            }
            return resultMap;
        }
    }
}
