package com.github.alexanderqwerty;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class Saver {
    public Saver() {
    }

    public Saver(String director) {
        this.director = director;
    }

    private String director = "C:\\Users\\duduc\\OneDrive\\Рабочий стол\\";


    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void getPhoto(MessageAttachment attachment) throws IOException {
        System.out.println(attachment.getPhoto().getSizes().get(attachment.getPhoto().getSizes().size() - 1).getUrl());
        URL url = new URL(attachment.getPhoto().getSizes().get(attachment.getPhoto().getSizes().size() - 1).getUrl().toString());
        Files.copy(url.openStream(), Paths.get(director + attachment.getPhoto().getId() + ".jpeg"), StandardCopyOption.REPLACE_EXISTING);
    }

    public void getVideo(VkApiClient vk, GroupActor a) throws IOException, ClientException, ApiException {
        vk.messages().send(a)
                .message("Не поддерживается скачивание видео")
                .randomId(new Random().nextInt(10000))
                .execute();
    }

    public void getAudio(MessageAttachment attachment) throws IOException {
        System.out.println(attachment.getAudio().getUrl());
        URL url = new URL(attachment.getAudio().getUrl().toString());
        Files.copy(url.openStream(), Paths.get(director + attachment.getAudio().getTitle() + ".mp3"), StandardCopyOption.REPLACE_EXISTING);
    }

    public void getDoc(MessageAttachment attachment) throws IOException {
        System.out.println(attachment.getDoc().getUrl());
        URL url = new URL(attachment.getDoc().getUrl().toString());
        Files.copy(url.openStream(), Paths.get(director + attachment.getDoc().getTitle()), StandardCopyOption.REPLACE_EXISTING);
    }

    public void getGraffiti(MessageAttachment attachment) throws IOException {
        System.out.println(attachment.getGraffiti().getUrl());
        URL url = new URL(attachment.getGraffiti().getUrl().toString());
        Files.copy(url.openStream(), Paths.get(director + attachment.getGraffiti().getId() + ".png"), StandardCopyOption.REPLACE_EXISTING);
    }

    public void getAudioMessage(MessageAttachment attachment) throws IOException {
        System.out.println(attachment.getAudioMessage().getLinkMp3());
        URL url = new URL(attachment.getAudioMessage().getLinkMp3().toString());
        Files.copy(url.openStream(), Paths.get(director + attachment.getAudioMessage().getId() + ".mp3"), StandardCopyOption.REPLACE_EXISTING);
    }

}
