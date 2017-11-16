package myProject.model.infoFromFile;

import myProject.Helper;

public class ShareSourceImpl implements FileSource {

    @Override
    public String getSourceFolder() {
//  todo может пригодиться
//        File[] files = new File(folder).listFiles();
//        List<File> result = new ArrayList<>();
//        if (files != null) {
//            for (File file : files) {
//                if (file.getName().matches("\\d{4}_\\d{2}_\\d{2}\\.log")) {
//                    result.add(file);
//                }
//            }
//            Collections.reverse(result);
//        }

        return Helper.getDefaultProperties().getString("logsFolder");
    }
}
