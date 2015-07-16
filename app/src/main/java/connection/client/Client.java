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

import connection.json.JSONException;
import connection.json.JSONObject;

public class Client {
    //    public static final String SERVER_URL = "http://localhost/java/virtual_wallet/index.php";
    public static final String SERVER_URL = "http://virtualwallet.sinaapp.com/";
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
//        Client client = Client.getClient();
//        debug(client.validate("yty", "yty"));
//
//        Response getBillsResponse = client.getBills();
//        if (getBillsResponse.getResult().equals("success")) {
//            debug(getBillsResponse.json.getJSONArray("bills").toString(4));
//        } else {
//            debug(getBillsResponse.getErrorMsg());
//        }
//        debug(client.getUserInfo());
//        debug(client.buyItem(1, 1));
//        debug(client.getUserBalance());
//        debug(client.createItem("item1", null, "item1", "item1 description", 20));
//        debug(client.getItemInfo("item1", null));
//        debug(client);
        test1(true);
    }

    public static void test1(boolean firstTime) {
        Client client = Client.getClient();
        if (firstTime) {
            debug(client.register("t1-admin", "t1-admin"));
        }
        debug(client.validate("t1-admin", "t1-admin"));
        if (firstTime) {
            debug(client.createItem("t1-item1", "t1-item1", "t1-item1 description", 1));
            debug(client.createItem("t1-item2", "t1-item2", "t1-item2 description", 1));
        }

        client.logout();
        // 切换到顾客
        if (firstTime) {
            debug(client.register("t1-user", "t1-user"));
        }
        debug(client.validate("t1-user", "t1-user"));
        Response itemInfo = client.getItemInfo("t1-item1");
        debug(itemInfo);
        if (itemInfo.getResult().equals("success")) {
            int itemId = itemInfo.json.getJSONObject("itemInfo").getInt("id");
            debug(client.buyItem(itemId, 1));
            debug(client.buyItem(itemId, 2));
        }
        debug(client.getBills());

    }

    /**
     * 注册一个用户
     *
     * @param username 用户名
     * @param password 密码
     * @return response
     */
    public Response register(String username, String password) {
        try {
            JSONObject content = packActionData("register",
                    new JSONObject().put("username", username).put("password", password));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
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
        if (response.getResult().equals("error")) { // 服务返回了非json串,json解析出错
            debug(response.getErrorMsg());
            debug(response.getRawString());
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
     * @return 返回包含用户信息的Response对象, 如果用户不存在或发生错误, 返回对应错误信息
     */
    public Response getUserInfo() {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        try {
            JSONObject content = packActionData("getUserInfo",
                    addUserInfo(new JSONObject()));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    public Response getBills() {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        try {
            JSONObject content = packActionData("getBills", addUserInfo(new JSONObject()));
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 获取用户的余额
     *
     * @return 出错返回-1,否则返回用户的余额(大于等于0)
     */
    public int getUserBalance() {
        Response response = getUserInfo();
        if (response == null || !response.getResult().equals("success")) {
            return -1;
        } else {
            return response.json.getJSONObject("userInfo").getInt("balance");
        }
    }

    /**
     * 创建物品单
     *
     * @return response
     */
    public Response createItem(String rawVal, String name, String description, int price) {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        JSONObject data = addUserInfo(new JSONObject()).put("rawVal", rawVal);
        data.put("name", name).put("description", description).put("price", price);
        JSONObject content = packActionData("createItem", data);

        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 购买物品
     *
     * @param itemId 购买的商品id
     * @param amount 购买数量
     * @return response
     */
    public Response buyItem(int itemId, int amount) {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        JSONObject data = addUserInfo(new JSONObject());
        data.put("itemId", itemId).put("amount", amount);
        JSONObject content = packActionData("buyItem", data);

        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }

    }

    /**
     * 获取物品单的信息
     */
    public Response getItemInfo(String rawVal) {
        JSONObject data = new JSONObject().put("rawVal", rawVal);
        JSONObject content = packActionData("getItemInfo", data);
        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
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

    public int getUserId() {
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

    // test function
    public String toString() {
        return "logined:" + logined + "\nuserId:" +
                userId + "\nusername:" + username + "\npassword:" + password;
    }

    // debug function
    static void debugToFile(String content) {
        File file = new File("C:\\Users\\sfc84\\.IdeaIC14\\config\\scratches\\scratch");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // debug function
    static void debug(Object... objects) {
        String seprator = "";
        for (Object object : objects) {
            if (object != null)
                System.out.print(seprator + object.toString());
            else
                System.out.print("null ");
            seprator = " ";
        }
        System.out.println();
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

    static class Prefab {
        static Response needValidate() {
            return new Response(new JSONObject().put("result", "error")
                    .put("errorMsg", "Call validate() first!").toString());
        }

        static Response connectionError() {
            return new Response(new JSONObject().put("result", "error")
                    .put("errorMsg", "Connection failed.").toString());
        }
    }

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
            result = "error";
            errorMsg = "Server responds a non-JSON string or an invalid JSON string.";
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
