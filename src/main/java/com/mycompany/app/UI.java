package com.mycompany.app;

import java.util.Scanner;

/**
 * Used to print options and other output
 */
public class UI {
    public Integer inputRange(String prompt, int min, int max) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);
        boolean done = false;
        Integer option = null;

        while (!done) {
            // get option for the initial entry point of the app
            option = Integer.parseInt(sc.next());
            
            if (min <= option && option <= max) {
                done = true;
            } else {
                System.out.println("Error: Invalid option");
            }
        }

        return option;
    }

    public String inputString(String prompt) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);
        String result = sc.nextLine();

        return result;
    }

    public void clearScreen() {
        System.out.print("\u001b[2J");
        System.out.flush();
    }
}
