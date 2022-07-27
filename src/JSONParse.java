import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.LinkedList;

public class JSONParse{
    public static String sendRequests(String https){
        try {
            URL url = new URL(https);
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));
            } catch (Exception e) {
                in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF8"));
            }
            String str[] ={""};
            in.lines().forEach((s)->str[0]+=s);;
            return str[0];
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static class JSONPair{
        String key;
        JSONValue value;
        public JSONPair(String key, JSONValue value){
            this.key = key;
            this.value = value;
        }
        public String getKey(){
            return key;
        }
        public JSONValue getValue(){
            return value;
        }
        @Override
        public String toString(){
            return key+":"+value;
        }

       public static JSONPair parse(String JSON){
            String key = JSON.substring(0, JSON.indexOf(":"));
            JSONValue value = JSONValue.parse(JSON.substring(JSON.indexOf(":")+1));
            return new JSONPair(key, value);
        }
    }

    public static class JSONMembers{
        LinkedList<JSONPair> members = new LinkedList<>(); 
        public JSONMembers(){}
        public JSONMembers(JSONPair...pair){
            for(JSONPair i:pair){
                members.add(i);
            }
        }
        @Override
        public String toString(){
            String str = "";
            for (JSONPair jsonPair : members) {
                str += ","+jsonPair.toString();
            }//Возвращает еще запятую перед всеми members
            if(members.size()>0){
                str = str.substring(1);//remove запятую mention above 
            }
            return str;
        }
        public void add(JSONPair pair){
            members.add(pair);
        }
        public JSONPair get(int id){
            return members.get(id);
        }
        public int size(){
            return members.size();
        }


        public static JSONMembers parse(String JSON){
            JSONMembers jMembers = new JSONMembers();
            while(JSON.length()>0){
                if(JSON.charAt(0)==','){
                    JSON = JSON.substring(1);
                }
                JSONPair pair = JSONPair.parse(JSON);
                jMembers.add(pair);
                String str = pair.toString();
                JSON = JSON.substring(str.length());
            }
            return jMembers;
        }
    }


    public static class JSONElements{
        LinkedList<JSONValue> elements = new LinkedList<>(); 
        public JSONElements(){}
        public JSONElements(JSONValue...elements){
            for(JSONValue i:elements){
                this.elements.add(i);
            }
        }
        public JSONValue get(int id){
            return elements.get(id);
        }
        public int size(){
            return elements.size();
        }
        @Override
        public String toString(){
            String str = "";
            for (JSONValue jsonPair : elements) {
                str += ","+jsonPair.toString();
            }//Возвращает еще запятую перед всеми members
            if(elements.size()>0){
                str = str.substring(1);//remove запятую mention above 
            }
            return "["+str+"]";
        }

        public void add(JSONValue pair){
            elements.add(pair);
        }
        static JSONElements parse(String JSON){
            JSONElements jElements = new JSONElements();
            while(JSON.length()>0){
                if(JSON.charAt(0)==','){
                    JSON = JSON.substring(1);
                }
                JSONValue pair = JSONValue.parse(JSON);
                jElements.add(pair);
                String str = pair.toString();
                JSON = JSON.substring(str.length());
            }
            return jElements;
        }
    }
    
    public static class JSONValue<T>{
        public Class<?> type;
        public T value;
        public JSONValue(T value){
            this.value = value;
            this.type = value.getClass();
        }
        public T get(Class<T> type){
            return type.cast(value);
        }

        @Override
        public String toString(){
            if(value.getClass().equals(String.class) ){
                if(value.equals("null")){
                    return "null";
                }
                return "\""+value.toString()+"\"";
            }
            return value.toString();
        }

        static JSONValue parse(String JSON){
            int skip=0; 
            switch(JSON.charAt(skip)){
                case '"':
                    int endJString = findEnd(JSON, '"');
                    String valueString = JSON.substring(1, endJString);
                    return new JSONValue<String>(valueString);
                case '{':
                    int endJObj = findEnd(JSON, '{');
                    JSONObject valueJObject = JSONObject.parse(JSON.substring(0, endJObj+1));
                    return new JSONValue<JSONObject>(valueJObject);
                case '[':
                    int endJArr = findEnd(JSON, '[');
                    JSONElements jArray  = JSONElements.parse(JSON.substring(1, endJArr));
                    return new JSONValue<JSONElements>(jArray);
                default:
                    if(JSON.startsWith("true")){
                        return new JSONValue<Boolean>(true);
                    }
                    if(JSON.startsWith("false")){
                        return new JSONValue<Boolean>(false);
                    }
                    if(JSON.startsWith("null")){
                        return new JSONValue<String>("null");
                    }
                    int i = 0;
                    String num = "0123456789.";
                    while(num.indexOf(JSON.charAt(i++))!= -1 && i<JSON.length()){
                        
                    }
                    String numerical;
                    if(i>=JSON.length()){
                        numerical = JSON.substring(0, i);
                    }else{
                        numerical = JSON.substring(0, i-1);
                    }
                    
                    try {
                        return new JSONValue<Integer>(Integer.parseInt(numerical));
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    try {
                        return new JSONValue<Double>(Double.parseDouble(numerical));
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    return null;
            } 
        }
    }
   
    public static class JSONObject{
        JSONMembers jMembers;
        public JSONObject(JSONMembers jMembers){
            this.jMembers = jMembers;
        }
        public JSONPair get(int id){
            return jMembers.members.get(id);
        }
        /**
         * get Pair by key
         * @param key pair
         * @return if success return JSON else not found return null
         */
        public JSONPair get(String key){
            key = "\""+key +"\"";
            for(JSONPair i:jMembers.members){
                if(i.key.equals(key)){
                    return i;
                }
            }
            return null;
        }
        public int size(){
            return jMembers.members.size();
        }

        public String getKeys(){
            String str = "";
            for (JSONPair i : jMembers.members) {
                str += i.key + "\n";
            }
            return str;
        }  
        
        public String getTypeWithKeys(){
            String str = "";
            for (JSONPair i : jMembers.members) {
                str += "key = "+ i.key + " type = " + i.value.type.getSimpleName() + "\n";
            }
            return str;
        }  

        @Override
        public String toString(){
            return "{"+jMembers.toString()+"}";
        }
        public static JSONObject parse(String JSON){
            JSON = JSON.substring(1, findEnd(JSON, '{'));
            return new JSONObject(JSONMembers.parse(JSON));
        }
    }

    private static int findEnd(String str, char start_sym){
        int begin = 0;
        int end = 0;
        int end_pos = 0;
        char end_sym = getEndSymbol(start_sym);
        if(start_sym == end_sym){
            for (int i=0; i<str.length(); i++){
                if(isSkipSymbol(str.charAt(i))){
                    i++;
                    continue;
                }
                if(str.charAt(i)==start_sym) begin++;    
                if(begin>1){
                    end_pos= i;
                    return end_pos;
                }
            }
        }
        for (int i=0; i<str.length(); i++){   
            if(isSkipSymbol(str.charAt(i))){
                i++;
                continue;
            }
            if(str.charAt(i)==start_sym) begin++;
            if(str.charAt(i)==end_sym) end++;
            if((begin == end)&&(end!=0)){
                end_pos = i;
                break;
            }
        }
        return end_pos;
    }

    private static boolean isSkipSymbol(char skipSym){
        return skipSym=='\\';
    }
    
    private static char getEndSymbol(char startSym){
        switch (startSym){
            case '{':
                return '}';
            case '"':
                return '"';
            case '[':
                return ']';
            case '\'':
                return '\'';
            default:
                return 0;
        }
    }
}

