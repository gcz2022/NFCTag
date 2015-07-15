package connection.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import connection.json.JSONArray;
import connection.json.JSONException;
import connection.json.JSONObject;


public class Client {
    public static final String SERVER_URL = "http://10.180.87.183//java/virtual_wallet/";
    private static Client singleton;

    private boolean logined;
    private String username, password;
    private int userId;

    private Client() {
        logined = false;
        username = null;
        password = null;
        userId = -1;
    }

    public static Client getClient() {
        if (singleton == null) {
            singleton = new Client();
        }
        return singleton;
    }

    // test function
    public static void main(String[] args) {
        Client client = Client.getClient();
//        debug(client.register("du3", "du5"));
//        debug(client.validate("du5", "du5"));
//        debug(client.register("yang", "yang"));
//        client.validate("yang", "yang");
//        client.getUserInfo();
//        client.validate("shinima", "shinima");
//        debug(client.getUserBalance());
//        debug(client.createItem("item0", null, "item0 description", 20));
        debug(client.getItemInfo("item0", null));
//        debug(client);
    }

    /**
     * 注册一个用户
     *
     * @param username 用户名
     * @param password 密码
     * @return response 如果response为null,表示发生了错误
     */
    public Response register(String username, String password) {
        try {
            JSONObject content = packActionData("register",
                    new JSONObject().put("username", username).put("password", password));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证用户名/密码是否正确
     *
     * @param username 用户名
     * @param password 密码
     * @return 如果验证通过, 返回true并设置相应的username,password, 否则返回false
     */
    public boolean validate(String username, String password) {
        Response response;
        try {
            JSONObject content = packActionData("validate",
                    new JSONObject().put("username", username).put("password", password));

            response = jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (response.json.getBoolean("valid")) { // 验证通过
            this.logined = true;
            this.username = username;
            this.password = password;
            this.userId = response.json.getInt("userId");
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        logined = false;
        username = null;
        password = null;
    }

    /**
     * 获取用户的信息
     *
     * @return 返回包含用户信息的Response对象, 如果用户不存在或发生错误, 返回null
     */
    public Response getUserInfo() {
        if (!logined)
            return null;
        try {
            JSONObject content = packActionData("getUserInfo",
                    addUserInfo(new JSONObject()));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取用户的余额
     *
     * @return 出错返回-1,否则返回用户的余额(大于等于0)
     */
    public int getUserBalance(Response response) {
        if (response == null || !response.getResult().equals("success")) {
            return -1;
        } else {
            return response.json.getJSONObject("userInfo").getInt("balance");
        }
    }

    public int getUserBalance() {
        return getUserBalance(getUserInfo());
    }

    /**
     * 创建物品单
     *
     * @return response 如果出错,则返回null
     */
    public Response createItem(String rawVal, String hashVal, String description, int price) {
        if (!logined)
            return null;
        JSONObject data = addUserInfo(new JSONObject());
        if (rawVal != null)
            data.put("rawVal", rawVal);
        if (hashVal != null)
            data.put("hashVal", hashVal);
        data.put("description", description).put("price", price);
        JSONObject content = packActionData("createItem", data);

        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取物品单的信息
     *
     * @return response 如果出错,则返回null
     */
    public Response getItemInfo(String rawVal, String hashVal) {
        JSONObject data = new JSONObject();
        if (rawVal != null)
            data.put("rawVal", rawVal);
        if (hashVal != null)
            data.put("hashVal", hashVal);
        JSONObject content = packActionData("getItemInfo", data);
        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isLogined() {
        return logined;
    }

    public String getUsername() {
        if (logined)
            return username;
        else
            return null;
    }

    public String getPassword() {
        if (logined)
            return password;
        else
            return null;
    }

    public int getuserId() {
        if (logined)
            return userId;
        else
            return -1;
    }


    private JSONObject addUserInfo(JSONObject data) {
        if (logined) {
            data.put("username", username).put("password", password).put("userId", userId);
        }
        return data;
    }

    public String toString() {
        return "logined:" + logined + "\nuserId:" +
                userId + "\nusername:" + username + "\npassword:" + password;
    }

    // debug function
    static void debugToFile(String content) throws IOException {
        FileWriter writer = new FileWriter(new File("C:\\Users\\sfc84\\.IdeaIC14\\config\\scratches\\scratch"));
        writer.write(content);
        writer.close();
    }

    // debug function
    static void debug(Object... objects) {
        for (Object object : objects) {
            if (object != null)
                System.out.print(object.toString() + " ");
            else
                System.out.print("null ");
        }
        System.out.println();
    }

    // test function
    static void test1() throws IOException {
        Client client = Client.getClient();
        JSONObject data = new JSONObject();
        data.put("aaa", "bbb").put("hello", "world");
        JSONArray array1 = new JSONArray();
        array1.put(1).put(2).put(3);
        data.put("array", array1);
        System.out.println(data.toString());
        System.out.println(client.jsonPost(data, Client.SERVER_URL));
    }

    Response jsonPost(JSONObject jsonObject, String serverUrl) throws IOException {
        return post(jsonEncode(jsonObject), serverUrl);
    }

    Response post(String content, String serverUrl) throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        // connection.connect(); // connection.setDoOutput(true)将调用connect(),故connect()可以省略
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(content);
        out.flush();
        out.close();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String result = readAllFromReader(reader);

        reader.close();
        connection.disconnect();

//        debugToFile(result);
        return new Response(result);
    }

    private static JSONObject packActionData(String action, JSONObject data) {
        return new JSONObject().put("action", action).put("data", data);
    }

    private static String jsonEncode(JSONObject jsonObject) throws IOException {
        Iterator<String> keys = jsonObject.keys();
        String key, seprator = "";
        Object value;
        StringBuilder result = new StringBuilder();
        while (keys.hasNext()) {
            key = keys.next();
            value = jsonObject.get(key);
            result.append(seprator).append(urlEncode(key))
                    .append("=").append(urlEncode(value.toString()));
            seprator = "&";
        }
        return result.toString();
    }

    private static String urlEncode(String string) throws IOException {
        return URLEncoder.encode(string, "utf-8");
    }

    private String readAllFromReader(BufferedReader reader) {
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}

class Response {
    private String result, errorMsg, rawString;
    public JSONObject json;

    public Response(String string) {
        rawString = string;
        try {
            json = new JSONObject(string);
            result = json.getString("result");
            if (result.equals("error")) {
                errorMsg = json.getString("errorMsg");
            }
        } catch (JSONException e) {
            json = null;
        }
    }

    public String getRawString() {
        return rawString;
    }

    public String getResult() {
        return result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String toString() {
        if (json != null)
            return json.toString(4);
        else
            return rawString;
    }
}

