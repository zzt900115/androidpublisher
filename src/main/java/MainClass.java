public class MainClass {
    public static void main (String args[]){

        BackUpApkInfo backUpApkInfo = new BackUpApkInfo();
        backUpApkInfo.getApkBackupLists(1,ApplicationConfig.APPLICATION_NAME,ApplicationConfig.PACKAGE_NAME);
    }
}
