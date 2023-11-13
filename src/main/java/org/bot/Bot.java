package org.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bot.dto.FundSchemes;
import org.bot.utils.PropertyUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class Bot extends TelegramLongPollingBot {
    private final PropertyUtils propertyUtils;

    public Bot(){
        propertyUtils = new PropertyUtils();
    }
    @Override
    public String getBotUsername() {
        return propertyUtils.getPropertyValue("telegram.bot.username");
    }
    @Override
    public String getBotToken() {
        return propertyUtils.getPropertyValue("telegram.bot.token");
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var username = user.getFirstName();
        System.out.println(username + " wrote " + msg.getText());

        if (msg.isCommand()) {
            if (msg.getText().equals("/scream"))
                scream(id, update.getMessage());
            else if (msg.getText().equals("/whisper"))
                copyMessage(id, msg.getMessageId());
            else if (msg.getText().equals("/allfunds")) {
                try {
                    String allFunds = getFundsList();
                    //System.out.println(allFunds);
                    ObjectMapper mapper = new ObjectMapper();
                    FundSchemes fundsSchemesList[] = mapper.readValue(allFunds, FundSchemes[].class);
                    System.out.println("Funds fetched. now printing");

                    for(FundSchemes fund : fundsSchemesList){
                        System.out.println(fund.getSchemeName());
                    }
                    sendText(id, "allfunds");
                } catch (IOException | InterruptedException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void scream(Long id, Message msg) {
        if (msg.hasText())
            sendText(id, msg.getText().toUpperCase());
        else
            copyMessage(id, msg.getMessageId());
    }

    private String getFundsList() throws IOException, InterruptedException {
        String url = "https://api.mfapi.in/mf";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.out.println(response.statusCode());
        return response.body();
    }
}
