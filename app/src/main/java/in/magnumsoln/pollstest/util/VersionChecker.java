package in.magnumsoln.pollstest.util;

import com.google.firebase.firestore.FirebaseFirestore;

public class VersionChecker {

    public static boolean isCompatibleVersion(String supportedVersion, String currentVersion) {
        try {
            int x = supportedVersion.indexOf('.');
            int y = currentVersion.indexOf('.');
            int majorSupportedVersion = Integer.parseInt(supportedVersion.substring(0, x));
            int majorCurrentVersion = Integer.parseInt(currentVersion.substring(0, y));
            int sprintCurrentVersion = Integer.parseInt(currentVersion.substring(x + 1));
            int sprintSupportedVersion = Integer.parseInt(supportedVersion.substring(y + 1));

            if (majorCurrentVersion < majorSupportedVersion)
                return false;
            else if (sprintCurrentVersion < sprintSupportedVersion)
                return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
