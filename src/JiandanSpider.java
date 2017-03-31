import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xuhao Chen on 2017/3/30.
 */
public class JiandanSpider {

    private static ArrayList<String> allResults = new ArrayList<>();

    private static void addResult(String line, ArrayList<String> allResults){
        String aResult = RegexString(line, "<a href=\"(//.+?)\".+?class=\"view_img_link\">");
        if(!aResult.equals("")) allResults.add(aResult);
    }

    private static String sendGet(String url){
        String result="";
        BufferedReader in = null;
        try{
            URL realURL = new URL(url);

            URLConnection connection = realURL.openConnection();
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while((line = in.readLine()) != null){
                addResult(line,allResults);
                result += line;
            }

        }catch(Exception e){
            System.out.println("GET Request failed.");
            e.printStackTrace();
        }

        finally {
            try{
                if(in != null) in.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String RegexString(String targetStr, String patternStr){
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(targetStr);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    private static void saveImg(String imgURL, String path){
        String[] tmp = imgURL.split("/");
        String fileName = tmp[tmp.length - 1];
        Path filePath = Paths.get(path + "/" + fileName);
        URL url;

        try{
            Files.createDirectories(Paths.get(path));
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            url = new URL(imgURL);
            InputStream in = url.openStream();
            Files.copy(in,filePath, StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args){

        String url = "http://jiandan.net/ooxx";

        System.out.println("Getting Info from: "+url);

        String result = sendGet(url);

        //Get latest page Number
        String pageNumberString = RegexString(result,"<span class=\"current-comment-page\">\\[(.+?)\\]");
        Integer latestPageNum = Integer.parseInt(pageNumberString);
        System.out.println("Latest page number: "+latestPageNum);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which mode would you wish to use?");
        System.out.println("1. Multiple Pages Mode");
        System.out.println("2. Specific Page Mode");
        switch(scanner.nextInt()){
            case 1:   //multiple pages mode
                System.out.print("Maximum Number of pages: ");
                Integer maxPage = scanner.nextInt();
                for(int i=0;i<maxPage;i++){
                    Integer currentPageNumber = latestPageNum - i;
                    System.out.println("Page "+currentPageNumber.toString()+":");
                    if(i != 0) {
                        allResults.clear();
                        sendGet(url + "/page-" + currentPageNumber.toString());   //if not the first page
                    }
                    System.out.println("Number of Result: "+allResults.size());
                    for(int j=0;j<allResults.size();j++){
                        System.out.print(allResults.get(j));
                        saveImg("http:"+allResults.get(j),"img/" + currentPageNumber.toString());
                        System.out.println("File Saved.");
                    }
                }
                break;

            case 2:  //specific page mode
                System.out.print("Please enter the Page Number:");
                Integer pageNum = scanner.nextInt();
                if(pageNum>latestPageNum || pageNum<0){
                    System.err.println("Wrong Page number!");
                }else{
                    if(pageNum != latestPageNum){
                        allResults.clear();
                        sendGet(url + "/page-" + pageNum.toString());
                    }
                    System.out.println("Number of Result: "+allResults.size());
                    for(int j=0;j<allResults.size();j++){
                        System.out.print(allResults.get(j));
                        saveImg("http:"+allResults.get(j),"img/"+pageNum.toString());
                        System.out.println("File Saved.");
                    }
                }
                break;
            default:
                System.err.println("Wrong Input.");
                break;

        }








    }
}
