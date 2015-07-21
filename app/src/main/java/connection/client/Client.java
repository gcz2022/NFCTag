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

    // 新浪服务器地址
    public static final String SERVER_URL = "http://virtualwallet.sinaapp.com/";
    // 本地调试地址
//    public static final String SERVER_URL = "http://10.180.32.116/java/virtualwallet/1/";
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

    /**
     * 取得client对象(单例)
     *
     * @return client对象
     */
    public static Client getClient() {
        if (singleton == null) {
            singleton = new Client();
        }
        return singleton;
    }

    public static void main(String[] args) {
//        clearDB();
//        initUsers();
//        test1(true);
//        test2(true);
//        test3();
        Client client = Client.getClient();
        client.validate("sfc", "sfc");
        client.validate("sfc", "sfc");
//        client.logout();
//        client.validate("admin", "admin");
//        client.getUserInfo();
//        client.getUserInfo();
//        client.recharge(1000);
//        client.getUserInfo();

    }

    private static void initUsers() {
        Client client = Client.getClient();
        client.register("sfc", "sfc");
        client.register("yty", "yty");
        client.register("dza", "dza");
        client.register("gcz", "gcz");
        client.register("admin", "admin");
    }

    // Very dangerous!!!
    private static void clearDB() {
        new Request().pack("clearDB").post(SERVER_URL);
    }

    /**
     * 模式1简单测试: 进行用户创建,商品单创建,商品购买,账单查询的基本测试
     *
     * @param firstTime 是否为第一次运行该测试
     */
    private static void test1(boolean firstTime) {
        Client client = Client.getClient();
        client.logout();

        // 切换到admin1
        if (firstTime) {
            client.register("t1-admin", "t1-admin");
            client.register("t1-admin", "t1-admin"); // duplicate register
            client.validate("t1-admin", "wrong-password"); // wrong password
            client.validate("t1-admin", "t1-admin");
            client.createItem("t1-item1", "t1-item1", "t1-item1 description", 1, 100);
            client.createItem("t1-item2", "t1-item2", "t1-item2 description", 3, 100);
            client.getItems();
        }
        // 切换到admin2
        if (firstTime) {
            client.logout();
            client.register("t1-admin2", "t1-admin2");
            client.validate("t1-admin2", "t1-admin2");
            client.createItem("t1-item3", "t1-item3", "t1-item3 description", 3, 100);
        }

        // 切换到user1
        client.logout();
        if (firstTime)
            client.register("t1-user", "t1-user");
        client.validate("t1-user", "t1-user");
        Response itemInfo1 = client.getItemInfo("t1-item1");
        if (itemInfo1.getResult().equals("success"))
            client.buyItem(itemInfo1.helper.getItemId(), 1);

        Response itemInfo2 = client.getItemInfo("t1-item2");
        if (itemInfo2.getResult().equals("success"))
            client.buyItem(itemInfo2.helper.getItemId(), 2);

        Response itemInfo3 = client.getItemInfo("t1-item3");
        if (itemInfo3.getResult().equals("success"))
            client.buyItem(itemInfo3.helper.getItemId(), 3);

        Response itemInfo4 = client.getItemInfo("t1-item4"); // non-exist item
        if (itemInfo4.getResult().equals("success"))
            client.buyItem(itemInfo4.helper.getItemId(), 3);

        client.getBills();
    }

    /**
     * 模式2简单测试: 进行钱包创建,钱包付款,钱包账单的基本测试
     *
     * @param firstTime 是否为第一次运行该测试
     */
    private static void test2(boolean firstTime) {
        Client client = Client.getClient();
        client.logout();

        // 切换到user
        if (firstTime) {
            client.register("t2-user", "t2-user");
        }
        client.validate("t2-user", "t2-user");
        client.createWallet("wallet1", "wallet1", "wallet1 description", 50);
        client.createWallet("wallet2", "wallet2", "wallet2 description", 30);
        client.createWallet("wallet3", "wallet3", "wallet3 description", 30); // money is not enough
        int[] walletIds = client.getWallets().helper.getWalletIds();

        // 切换到admin
        client.logout();
        if (firstTime) {
            client.register("t2-admin", "t2-admin");
        }
        client.validate("t2-admin", "t2-admin");
        client.charge("wallet1", 30);
        client.charge("wallet1", 30); // money in wallet1 is not enough
        client.charge("wallet4", 10); // wallet does not exist

        client.getWalletBills(walletIds[0]); // wallet does not belong to admin
        client.logout();
        client.validate("t2-user", "t2-user");
        client.getWalletBills(walletIds[0]);
    }

    private static void test3() {
        Client client = Client.getClient();
        client.validate("sfc", "sfc");
        client.buyItem(1, 10);
        client.getBills();

        client.logout();
        client.validate("t1-admin", "t1-admin");
        client.getItemInfo("t1-item1");
        client.getBills();
    }

    /**
     * 注册一个用户
     *
     * @param username 用户名
     * @param password 密码
     * @return response
     */
    public Response register(String username, String password) {
        return new Request().requireLogout()
                .put("username", username).put("password", password)
                .pack("register")
                .post(SERVER_URL);
    }

    /**
     * 验证用户名/密码是否正确
     *
     * @param username 用户名
     * @param password 密码
     * @return response 如果验证通过,将会设置相应的username,password,logined
     */
    public Response validate(String username, String password) {
        Response response = new Request().requireLogout()
                .put("username", username)
                .put("password", password)
                .pack("validate").post(SERVER_URL);
        if (response.getResult().equals("success")) {
            this.logined = true;
            this.username = username;
            this.password = password;
            this.userId = response.json.getInt("userId");
        }
        return response;
    }

    /**
     * 登出
     */
    public void logout() {
        logined = false;
        username = null;
        password = null;
    }

    /**
     * 获取用户的信息
     *
     * @return response
     */
    public Response getUserInfo() {
        return new Request().requireLogin().pack("getUserInfo").post(SERVER_URL);
    }

    /**
     * 获取用户的账单
     *
     * @return response
     */
    public Response getBills() {
        return new Request().requireLogin().pack("getBills").post(SERVER_URL);
    }

    /**
     * 创建物品单
     *
     * @param rawVal      物品单标识码
     * @param name        物品名称
     * @param description 物品描述
     * @param price       物品单价
     * @return response
     */
    public Response createItem(String rawVal, String name,
                               String description, int price, int initial) {
        return new Request().requireLogin()
                .put("rawVal", rawVal)
                .put("name", name)
                .put("description", description)
                .put("price", price)
                .put("initial", initial)
                .pack("createItem").post(SERVER_URL);
    }

    /**
     * 购买物品
     *
     * @param itemId 购买的商品id
     * @param amount 购买数量
     * @return response
     */
    public Response buyItem(int itemId, int amount) {
        return new Request().requireLogin()
                .put("itemId", itemId)
                .put("amount", amount)
                .pack("buyItem").post(SERVER_URL);
    }

    /**
     * 获取物品单的信息
     *
     * @param rawVal 物品的标识码
     * @return response
     */
    public Response getItemInfo(String rawVal) {
        return new Request()
                .put("rawVal", rawVal)
                .pack("getItemInfo").post(SERVER_URL);
    }

    /**
     * 获取用户的所有钱包信息
     *
     * @return response
     */
    public Response getWallets() {
        return new Request().requireLogin().pack("getWallets").post(SERVER_URL);
    }

    /**
     * 获取用户的所有商品信息
     *
     * @return response
     */
    public Response getItems() {
        return new Request().requireLogin().pack("getItems").post(SERVER_URL);
    }

    /**
     * 获取用户某个钱包的账单
     *
     * @param walletId 钱包id
     * @return response
     */
    public Response getWalletBills(int walletId) {
        return new Request().requireLogin()
                .put("walletId", walletId)
                .pack("getWalletBills").post(SERVER_URL);
    }

    /**
     * 商家向用户收费
     *
     * @param rawVal     钱包的标识码
     * @param totalPrice 收费总额
     * @return response
     */
    public Response charge(String rawVal, int totalPrice) {
        return new Request().requireLogin()
                .put("rawVal", rawVal)
                .put("totalPrice", totalPrice)
                .pack("charge").post(SERVER_URL);
    }

    /**
     * 用户进行帐户充值
     *
     * @param amount 充值金额
     * @return response
     */
    public Response recharge(int amount) {
        return new Request().requireLogin()
                .put("amount", amount)
                .pack("recharge").post(SERVER_URL);
    }

    /**
     * 新建一个虚拟钱包
     *
     * @param name        钱包名称
     * @param rawVal      钱包的标识码
     * @param description 钱包描述
     * @param balance     钱包初始金额
     * @return response
     */
    public Response createWallet(String name, String rawVal, String description, int balance) {
        return new Request().requireLogin()
                .put("name", name)
                .put("rawVal", rawVal)
                .put("description", description)
                .put("balance", balance)
                .pack("createWallet").post(SERVER_URL);
    }

    /**
     * 删除一个钱包
     *
     * @param walletId 钱包id
     * @return response
     */
    public Response deleteWallet(int walletId) {
        return new Request().requireLogin()
                .put("walletId", walletId)
                .pack("deleteWallet").post(SERVER_URL);
    }

    /**
     * 删除一个商品单
     *
     * @param itemId 商品单id
     * @return response
     */
    public Response deleteItem(int itemId) {
        return new Request().requireLogin()
                .put("itemId", itemId)
                .pack("deleteItem").post(SERVER_URL);
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

    // debug function
    private static boolean lastWriteFail = false;
    private static final String debugFile = "C:\\Users\\sfc84\\.IdeaIC14\\config\\scratches\\scratch";

    private static void debugToFile(String content) {
        if (lastWriteFail) // 第一次写失败之后就不再尝试
            return;
        File file = new File(debugFile);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            lastWriteFail = true;
        }
    }

    // debug function
    public static void debug(Object... objects) {
        String seprator = "";
        for (Object object : objects) {
            if (object != null)
                System.out.print(seprator + object.toString());
            else
                System.out.print(seprator + "null");
            seprator = " ";
        }
        System.out.println();
    }

    private Response jsonPost(JSONObject jsonObject, String serverUrl) {
        try {
            return post(jsonEncode(jsonObject), serverUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    private Response post(String content, String serverUrl) throws IOException {
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

        debugToFile(result);
        return new Response(result);
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

    public abstract static class AsnyRequest implements Runnable {
        private Response response;

        public void run() {
            response = getResponse();
        }

        public abstract Response getResponse();

        /**
         * 发送一个异步请求
         *
         * @return 请求对应的响应
         */
        public Response post() {
            Thread thread = new Thread(this);
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    public static class Request {
        private JSONObject json;
        private Client client;
        private boolean isUserInfoAdded, requireLoginFail, requireLogoutFail, hasError;

        public Request() {
            this.json = new JSONObject();
            this.client = Client.getClient();
            isUserInfoAdded = false;
            requireLoginFail = false;
            requireLogoutFail = false;
            hasError = false;
        }

        public Response post(String serverUrl) {
            if (requireLoginFail)
                return Response.Prefab.needValidate();
            else if (requireLogoutFail)
                return Response.Prefab.needLogout();
            else
                return client.jsonPost(json, serverUrl);
        }

        Request addUserInfo() {
            if (hasError)
                return this;
            if (client.isLogined()) {
                put("username", client.getUsername());
                put("password", client.getPassword());
                put("userId", client.getUserId());
            }
            isUserInfoAdded = true;
            return this;
        }

        Request pack(String action) {
            if (hasError)
                return this;
            if (!isUserInfoAdded) {
                addUserInfo();
            }
            return wrap("data").put("action", action);
        }

        Request wrap(String key) {
            if (hasError) {
                return this;
            } else {
                return new Request().put(key, json);
            }
        }

        Request put(String key, String value) {
            if (!hasError) {
                json.put(key, value);
            }
            return this;
        }

        Request put(String key, Object value) {
            if (!hasError) {
                json.put(key, value);
            }
            return this;
        }

        Request put(String key, int value) {
            if (!hasError) {
                json.put(key, value);
            }
            return this;
        }

        Request requireLogin() {
            if (hasError)
                return this;
            if (!client.isLogined()) {
                requireLoginFail = true;
                hasError = true;
            }
            return this;
        }

        Request requireLogout() {
            if (hasError)
                return this;
            if (client.isLogined()) {
                requireLogoutFail = true;
                hasError = true;
            }
            return this;
        }
    }

    public static class Response {
        /**
         * 是否开启自动调试, 开启后所有的request和response都会被打印出来
         */
        public static final boolean autoDebug = true;

        /**
         * 用于从response对象中方便的提取需要的数据<br>
         * <strong>注意:</strong>确保在正确的情况下调用正确的helper函数
         */
        public Helper helper;

        private String result, errorMsg, rawString;
        public JSONObject json;

        static class Prefab {
            static Response needValidate() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "Call validate() first!").toString());
            }

            static Response connectionError() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "Cannot connect to server.").toString());
            }

            static Response needLogout() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "You are alread validated, call logout() before call validate().").toString());
            }
        }

        public class Helper {
            /**
             * 获取用户的余额 for getUserInfo
             */
            public int getUserBalance() {
                return json.getJSONObject("userInfo").getInt("balance");
            }

            /**
             * for getItemInfo
             */
            public int getItemId() {
                return json.getJSONObject("itemInfo").getInt("id");
            }

            /**
             * for getWallets
             */
            public int[] getWalletIds() {
                JSONArray wallets = json.getJSONArray("wallets");
                int[] result = new int[wallets.length()];
                for (int i = 0; i < wallets.length(); i++) {
                    result[i] = wallets.getJSONObject(i).getInt("id");
                }
                return result;
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
                errorMsg = "Server responds a non-JSON string or an invalid JSON string. "
                        + "Maybe you connect to ZJUWALN but has not logined.";
            }
            if (autoDebug)
                Client.debug(this);
            helper = new Helper();
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

}
