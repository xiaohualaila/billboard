package cn.com.billboard.util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.com.library.kit.Kits;

public class FileUtil {
    public static List<String> getFilePath(String path){
        List<String> list= new ArrayList<>();
        list.clear();
        File file = new File(path);
        if(file!=null){
            File[] fs =  file.listFiles();
            if(fs!=null&&fs.length>0){
                for(File f:fs) {
                    if (!f.isDirectory()) {
                        list.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return list;
    }


    public static List<String>  getCommonFileNames(List<String> fs_url,String file_name){
        List<String> old_videos = FileUtil.getFilePath(file_name);
        if(fs_url.size()>0 && fs_url!=null){

            File file1 = null;
            List<String> remove_v_list = new ArrayList<>();
            List<String> remove_v_list2 = new ArrayList<>();
            if(old_videos.size()>0){
                if(fs_url.size()>0){
                    for(int i=0;i<old_videos.size();i++){
                        file1= new File(old_videos.get(i));
                        for(int j=0;j<fs_url.size();j++){
                            int index = fs_url.get(j).lastIndexOf("/");
                            String str = fs_url.get(j).substring(index+1,fs_url.get(j).length());

                            String name = file1.getName();

                            if(name.equals(str)){//存在相同的文件
                                remove_v_list.add(old_videos.get(i));
                                remove_v_list2.add(fs_url.get(j));
                            }
                        }
                    }
                    old_videos.removeAll(remove_v_list);
                    fs_url.removeAll(remove_v_list2);
                    if(old_videos.size()>0){
                        for(String old_v :old_videos ){
                            File old_f =  new File(old_v);
                            if(old_f!=null){
                                old_f.delete();
                            }
                        }
                    }
                }
            }

        }else {
            if(old_videos.size()>0){
                Kits.File.deleteFile(file_name);
            }
        }
        return fs_url;
    }
}
