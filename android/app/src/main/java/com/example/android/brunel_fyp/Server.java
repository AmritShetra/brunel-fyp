package com.example.android.brunel_fyp;

import io.github.cdimascio.dotenv.Dotenv;

class Server {

    private static Dotenv dotenv = Dotenv.configure()
            .directory("./assets")
            .filename("env")
            .load();

    private static String address = dotenv.get("SERVER_ADDRESS");

    static String loginRoute() { return address + "/login/"; }
    static String registerRoute() { return address + "/register/"; }
    static String profileRoute() { return address + "/users/"; }
    static String chatbotRoute() { return address + "/classify/"; }
}
