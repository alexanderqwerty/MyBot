package com.github.alexanderqwerty;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;

import javax.print.PrintException;
import java.io.IOException;
import java.util.*;

public class Main {
    static final String accessToken = "4da688544537d33cd6f120a4cef3e678b84074bae59038ed9729db382f697395c418802676078551790e7";

    static final Integer groupId = 213452188;
    static final KeyboardButton[][] arrChoseActionButtons = {{new KeyboardButton().setAction(new KeyboardButtonAction().setLabel("Print photo").setType(TemplateActionTypeNames.TEXT))},
            {new KeyboardButton().setAction(new KeyboardButtonAction().setLabel("Save something").setType(TemplateActionTypeNames.TEXT))}};
    static final KeyboardButton[][] arrRedStopButton = {{new KeyboardButton().setAction(new KeyboardButtonAction().setLabel("Stop").setType(TemplateActionTypeNames.TEXT)).setColor(KeyboardButtonColor.NEGATIVE)}};

    public static void sendUserMessage(VkApiClient vk, Message inputMessage, GroupActor actor, String sendMessage) {
        try {
            vk.messages().send(actor)
                    .message(sendMessage)
                    .userId(inputMessage.getFromId())
                    .randomId(new Random().nextInt(10000))
                    .execute();
        } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendUserMessageAndKeyboard(VkApiClient vk, Message inputMessage, GroupActor actor, String sendMessage, Keyboard keyboard) {
        try {
            vk.messages().send(actor)
                    .message(sendMessage)
                    .userId(inputMessage.getFromId())
                    .randomId(new Random().nextInt(10000))
                    .keyboard(keyboard)
                    .execute();
        } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasReplyMessage(Message message) {
        try {
            return !message.getReplyMessage().getAttachments().isEmpty();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void downloader(MessageAttachment attachment, VkApiClient vk, GroupActor actor, Message message) {
        Saver saver = new Saver();
        try {
            switch (attachment.getType()) {
                case PHOTO -> saver.getPhoto(attachment);
                case AUDIO -> saver.getAudio(attachment);
                case DOC -> saver.getDoc(attachment);
                case GRAFFITI -> saver.getGraffiti(attachment);
                case AUDIO_MESSAGE -> saver.getAudioMessage(attachment);
                default -> vk.messages().send(actor).userId(message.getId())
                        .randomId(new Random().nextInt(10000))
                        .message("Пока не поддерживается")
                        .execute();
            }
        } catch (IOException | ClientException | ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<List<T>> matrixToListOfLists(T[][] matrix) {
        List<List<T>> ans = new ArrayList<>();
        for (T[] row : matrix) {
            ans.add(Arrays.asList(row));
        }
        return ans;
    }

    public static void main(String[] args) throws ClientException, ApiException, InterruptedException {
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        GroupActor actor = new GroupActor(groupId, accessToken);
        System.out.println(vk.messages().getLongPollServer(actor).execute().toString());
        Integer ts = vk.messages().getLongPollServer(actor).execute().getTs();
        List<List<KeyboardButton>> redStopButton = matrixToListOfLists(arrRedStopButton);
        List<List<KeyboardButton>> choseActionButtons = matrixToListOfLists(arrChoseActionButtons);
        
        while (true) {
            MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(actor).ts(ts);
            List<Message> messages = historyQuery.execute().getMessages().getItems();
            if (!messages.isEmpty()) {
                messages.forEach(message -> {
                    switch (message.getText()) {
                        case "Print photo" -> {
                            sendUserMessageAndKeyboard(vk, message, actor,
                                    "Чтобы закончить напишите стоп",
                                    new Keyboard().setButtons(redStopButton));
                            outLoop:
                            while (true) {
                                try {
                                    Integer printTs = vk.messages().getLongPollServer(actor).execute().getTs();
                                    MessagesGetLongPollHistoryQuery printQuery = vk.messages().getLongPollHistory(actor).ts(printTs);
                                    List<Message> printMessages = printQuery.execute().getMessages().getItems();
                                    if (!printMessages.isEmpty()) {
                                        for (Message printMessage : printMessages) {
                                            if (!printMessage.getAttachments().isEmpty()) {
                                                Printer printer = new Printer();
                                                printMessage.getAttachments().forEach(printer::print);
                                            } else if (!printMessage.getReplyMessage().getAttachments().isEmpty()) {
                                                Printer printer = new Printer();
                                                printMessage.getReplyMessage().getAttachments().forEach(printer::print);
                                            } else if (printMessage.getText().equalsIgnoreCase("stop")) {
                                                break outLoop;
                                            } else {
                                                vk.messages().send(actor)
                                                        .message("Я не понимаю что вы хотите от меня")
                                                        .randomId(new Random().nextInt(1000))
                                                        .userId(message.getFromId())
                                                        .execute();
                                            }

                                        }
                                    }

                                } catch (ApiException | ClientException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        case "Save something" -> {
                            sendUserMessageAndKeyboard(vk, message, actor,
                                    "Чтобы закончить напишите стоп",
                                    new Keyboard().setButtons(redStopButton));
                            outLoop:
                            while (true) {
                                try {
                                    Integer downloadTs = vk.messages().getLongPollServer(actor).execute().getTs();
                                    MessagesGetLongPollHistoryQuery downloadQuery = vk.messages().getLongPollHistory(actor).ts(downloadTs);
                                    List<Message> downloadMessages = downloadQuery.execute().getMessages().getItems();
                                    if (!downloadMessages.isEmpty()) {
                                        for (Message downloadMessage : downloadMessages) {
                                            if (!downloadMessage.getAttachments().isEmpty()) {
                                                downloadMessage.getAttachments().forEach(downloadAttachment -> {
                                                    downloader(downloadAttachment, vk, actor, downloadMessage);
                                                });
                                            } else if (hasReplyMessage(downloadMessage)) {
                                                if (!downloadMessage.getReplyMessage().getAttachments().isEmpty()) {
                                                    downloadMessage.getReplyMessage().getAttachments().forEach(downloadAttachment -> {
                                                        downloader(downloadAttachment, vk, actor, downloadMessage);
                                                    });
                                                }
                                            } else if (downloadMessage.getText().equalsIgnoreCase("stop")) {
                                                break outLoop;
                                            } else {
                                                vk.messages().send(actor)
                                                        .message("Я не понимаю что вы хотите от меня")
                                                        .randomId(new Random().nextInt(1000))
                                                        .userId(message.getFromId())
                                                        .execute();
                                            }
                                        }
                                    }
                                    Thread.sleep(500);
                                } catch (ApiException | ClientException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        default -> sendUserMessageAndKeyboard(vk, message, actor,
                                "Это клавиатура с командами которые я могу исполнить",
                                new Keyboard().setButtons(choseActionButtons).setOneTime(true));

                    }
                });
            }
            ts = vk.messages().getLongPollServer(actor).execute().getTs();
            Thread.sleep(500);
        }
    }
}