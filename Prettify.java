import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.PrintWriter;

public class Prettify {

    public static void main(String[] args) {
        FileSuite fs = new FileSuite();
        ArrayList<PrettifyToken> ts = fs.readFile(args[0]);
        System.out.println("Converting File: " + args[0]);
        if(ts != null){
          System.out.println("Starting to parse curly brackets...");
          ts = (new CurlyReducer(ts)).reduce();
          System.out.println("Starting to reduce white space...");
          ts = (new WhiteSpaceReducer(ts)).reduce();
          System.out.println("Starting to inject whitespace metadata...");
          ts = (new IndentReducer(ts)).reduce();
          System.out.println("Starting to convet token array to string...");
          String s = (new Interpreter(ts)).interpretTokens();
          fs.createFile((args[0]).substring(0,args[0].length()-5)+"_converted.java",s);
          System.out.println("Converting Done.");
        }
    }

}

class FileSuite {
  public FileSuite() {
  }
  public ArrayList<PrettifyToken> readFile(String filename) {
    File file = new File(filename);
    try {
        Scanner sc = new Scanner(file);
        ArrayList<PrettifyToken> fileContent = new ArrayList<PrettifyToken>();
        while (sc.hasNextLine()) {
            fileContent.add(new PrettifyToken(sc.nextLine()));
        }
        sc.close();
        return fileContent;
    }
    catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    return null;
  }
  public void createFile(String filename, String content) {
    try{
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println(content);
        writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class Token {
  private String rawContent;

  public Token(String ct) {
    rawContent = ct;
  }
  public String getRawContent() {
    return rawContent;
  }
}

class PrettifyToken extends Token {
  private int indentCt;
  public PrettifyToken(String ct) {
    super(ct);
    indentCt = 0;
  }
  public void setIndentCt(int i) {
    indentCt = i;
  }
  public int getIndentCt() {
    return indentCt;
  }
}

abstract class TokenReducer {
  abstract ArrayList<PrettifyToken> reduce();
}

class CurlyReducer extends TokenReducer {
  private ArrayList<PrettifyToken> content;
  private ArrayList<PrettifyToken> newContent;

  public CurlyReducer(ArrayList<PrettifyToken> ct) {
    content = ct;
    newContent = new ArrayList<PrettifyToken>();
  }
  public ArrayList<PrettifyToken> curlyInspect(PrettifyToken t) {
    ArrayList<PrettifyToken> newTokens = new ArrayList<PrettifyToken>();
    String raw = t.getRawContent();
    String newRaw = "";
    for(char c : raw.toCharArray()) {
      if(c=='}' || c == '{') {
        newTokens.add(new PrettifyToken(newRaw));
        newTokens.add(new PrettifyToken(c+""));
        newRaw = "";
      } else {
        newRaw += c;
      }
    }
    if(newRaw != "")
      newTokens.add(new PrettifyToken(newRaw));
    return newTokens;
  }

  public ArrayList<PrettifyToken> reduce(){
    for(PrettifyToken t:content){
      newContent.addAll(curlyInspect(t));
    }
    return newContent;
  }
}

class IndentReducer extends TokenReducer {
  private ArrayList<PrettifyToken> content;
  private ArrayList<PrettifyToken> newContent;
  private int globalIndent;
  public IndentReducer(ArrayList<PrettifyToken> ct) {
    content = ct;
    globalIndent = 0;
    newContent = new ArrayList<PrettifyToken>();
  }

  public PrettifyToken indentInspector(PrettifyToken t) {
    PrettifyToken newToken = new PrettifyToken(t.getRawContent());
    newToken.setIndentCt(globalIndent);
    if(t.getRawContent().equals("}")) {
      globalIndent--;
      newToken.setIndentCt(globalIndent);
    }
    if(t.getRawContent().equals("{"))
      globalIndent++;

    return newToken;
  }

  public ArrayList<PrettifyToken> reduce(){
    for(PrettifyToken t:content){
      newContent.add(indentInspector(t));
    }
    return newContent;
  }
}

class WhiteSpaceReducer extends TokenReducer {
  private ArrayList<PrettifyToken> content;
  private ArrayList<PrettifyToken> newContent;
  private int globalIndent;
  public WhiteSpaceReducer(ArrayList<PrettifyToken> ct) {
    content = ct;
    newContent = new ArrayList<PrettifyToken>();
  }

  public ArrayList<PrettifyToken> reduce(){
    for(PrettifyToken t:content){
      String newRaw = t.getRawContent().trim();
      if(!newRaw.equals(""))
        newContent.add(new PrettifyToken(newRaw));
    }
    return newContent;
  }
}

class Interpreter {
  private String finalString;
  private ArrayList<PrettifyToken> initTokens;

  public Interpreter(ArrayList<PrettifyToken> tokens) {
    initTokens = tokens;
    finalString = "";
  }
  public String tokenToString(PrettifyToken t) {
    String s = "";
    for(int i = 0; i<t.getIndentCt();i++)
      s+="\t";
    s+=t.getRawContent();
    s+="\n";
    return s;
  }

  public String interpretTokens() {
    for(PrettifyToken t : initTokens) {
      finalString += tokenToString(t);
    }
    return finalString;
  }
}
