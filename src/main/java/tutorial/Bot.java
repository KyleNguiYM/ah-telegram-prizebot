package tutorial;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "AH_Prizebot";
    }

    @Override
    public String getBotToken() {
        return "6878030527:AAEQYw7cpheD7jAre3BPqqajYcYxj9iGcUE";
    }

    // Add a variable to keep track of the current step
    private enum GiveawayStep {
        ENTER_TITLE,
        SETUP_PARTICIPATE_EMOJI,
        ENTER_DESCRIPTION,
        SETUP_PARTICIPANTS,
        SETUP_WINNERS,
        SETUP_CONDITION,
        SETUP_DATE,
        //UPLOAD_MEDIA,
        NO_STATE
        // Add more steps as needed

    }

    private enum GiveawayInfo {;
        private Set<Long> participants = new HashSet<>();

        public void addParticipant(long userId) {
            participants.add(userId);
        }

        public boolean hasParticipant(long userId) {
            return participants.contains(userId);
        }

        public int getParticipantCount() {
            return participants.size();
        }

    }

    private enum GiveawayMedia {;
        private Set<PhotoSize> photo = new HashSet<>();
        private Set<Video> video = new HashSet<>();
        public boolean getPhoto(PhotoSize photoSize) {
            return photo.add(photoSize);
        }
        public Video getVideo(Video video) {
            return video;
        }

    }

    private Map<Long, GiveawayStep> giveawayCreationState = new HashMap<>();
    private Map<Long, String> giveawayTitleMap = new HashMap<>();
    private Map<Long, String> giveawayDescriptionMap = new HashMap<>();
    private Map<Long, Integer> giveawayParticipantsMap = new HashMap<>();
    private Map<Long, Integer> giveawayWinnersMap = new HashMap<>();
    private Map<Long, String> giveawayEmojiMap = new HashMap<>();
    private Map<Long, String> giveawayConditionMap = new HashMap<>();
    private Map<Long, String> giveawayDateMap = new HashMap<>();
    private Map<Long, String> giveawayMediaMap = new HashMap<>();
    private Map<String, GiveawayInfo> giveawaysMap = new HashMap<>();
    private Map<Long, List<String>> userGiveawaysMap = new HashMap<>();
    Map<String, List<String>> userGiveawaysMemberMap = new HashMap<>();
    public void storeText(long id, String text) {
        List<String> texts = new ArrayList<>();
        texts.add(text);
        userGiveawaysMap.put(id, texts);
    }
    public void storeMedia(long id, String file_id) {
        List<String> media = new ArrayList<>();
        media.add(file_id);
        giveawayMediaMap.put(id, media.toString());
    }

    public List<Map.Entry<Long, List<String>>> getAllDataFromMap() {
        return new ArrayList<>(userGiveawaysMap.entrySet());
    }

    public List<Map.Entry<Long, String>> getMediaFromMap() {
        return new ArrayList<>(giveawayMediaMap.entrySet());
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton shareButton = new InlineKeyboardButton();
        shareButton.setText("Share My Profile");
        shareButton.setSwitchInlineQuery("Send my profile to:");
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(shareButton);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    List <String> giveawayMemberList = new ArrayList<>();
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            // Handle callback queries
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();
            int msgId = callbackQuery.getMessage().getMessageId();
            User user = callbackQuery.getFrom();
            String username = user.getUserName();
            String firstname =user.getFirstName();
            int i = 1;

            if (callbackData.equals("english")) {
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("You selected English as the language.");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("chinese")) {
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("你选择了中文作为语言。");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("malay")) {
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText("Anda memilih Bahasa Malaysia sebagai bahasa.");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("join_giveaway")) {
                // Perform actions when the user clicks the emoji button
                // For example, add the user to the giveaway participants
                String giveawayTitle = giveawayTitleMap.toString(); // Assuming the title is in the message text
                giveawaysMap.computeIfPresent(giveawayTitle, (title, info) -> {
                    info.addParticipant(chatId); // Add the user to the participants
                    return info;
                });

                // Respond to the button click (e.g., display a confirmation message)
                SendMessage response = new SendMessage();
                SendMessage response2 = new SendMessage();

                if (userGiveawaysMemberMap.containsKey(giveawayTitle)) {
                    giveawayMemberList = userGiveawaysMemberMap.get(giveawayTitle);
                    if (giveawayMemberList.contains(firstname)) {
                        // User has already joined, send a reminder message
                        SendMessage reminder = new SendMessage();
                        reminder.setChatId(chatId);
                        reminder.setText("You've already joined the giveaway!");
                        try {
                            execute(reminder);
                            return; // Exit the method to prevent adding the user again
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // Add the user to the list
                giveawayMemberList.add(firstname);
                userGiveawaysMemberMap.put(giveawayTitle, giveawayMemberList); // Update the map with the updated list

                response.setChatId(chatId);
                response.setText("You've joined the giveaway!");
                System.out.println("User " + firstname + " ("+ username + ") clicked join button");

                response2.setChatId(chatId);
                response2.setText("Members joined (" + giveawayMemberList.size() + "/" + giveawayParticipantsMap.get(chatId) +
                        "):\n" + String.join("\n", giveawayMemberList)); // Join the list elements with newline separator

                try {
                    execute(response);
                    execute(response2);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("delete")){
                var id = user.getId();
                giveawayTitleMap.remove(id);
                giveawayEmojiMap.remove(id);
                giveawayDateMap.remove(id);
                userGiveawaysMap.remove(1L);
                userGiveawaysMap.clear();
                //updateMessageAfterDeletion(chatId,msgId);
                EditMessageText deleteMessage = new EditMessageText();
                deleteMessage.setChatId(chatId);
                deleteMessage.setMessageId(msgId);
                /*SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("Your giveaway has been deleted.");*/
                deleteMessage.setText("Your giveaway has been deleted.");
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("cancel_add")){
                giveawayMediaMap.remove(1L);
                giveawayMediaMap.clear();
                SendMessage cancelMessage = new SendMessage();
                cancelMessage.setChatId(chatId);
                cancelMessage.setText("Upload media step cancelled.");
                try {
                    execute(cancelMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("add_photo_" + i)){
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("Please upload a picture for the giveaway.");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("add_video_" + i)){
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("Please upload a video for the giveaway.");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("raffle")){
                long groupId = update.getMessage().getChat().getId();
                conductRaffle(groupId);
            } else if (callbackData.equals("share")) {
                // Forward the giveaway message
                //forwardGiveawayMessage(update);
                var id = user.getId();
                SendMessage sm = new SendMessage();
                sm.setChatId(id.toString());
                sm.setText("You've share profile");
                sm.setReplyMarkup(createInlineKeyboard());
                List<Map.Entry<Long, List<String>>> allData = getAllDataFromMap();
                StringBuilder messageText = new StringBuilder("Giveaway:\n");
//                int i = 1;
                for (Map.Entry<Long, List<String>> entry : allData) {
                    String giveawayDetails = entry.getValue().get(0); // Assuming only one detail for simplicity

                    messageText.append(giveawayDetails).append("\n\n");
                    i++;
                }

                try {
                    System.out.println("shared a profile");
                    execute(sm);
//                    System.out.println(messageText);

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            var msg = update.getMessage();
            var user = msg.getFrom();
            var id = user.getId();
            var chatId = msg.getChatId();

//            var url = InlineKeyboardButton.builder()
//                    .text("Tutorial")
//                    .url("https://core.telegram.org/bots/api")
//                    .build();

            var txt = msg.getText();
            var pht = msg.hasPhoto();
            var vid = msg.hasVideo();
            // Get the photo message
            List<PhotoSize> photos = update.getMessage().getPhoto();
            // Get the video message
            Video video = msg.getVideo();
            String f_id = null;

            if(msg.isCommand() || msg.isUserMessage() || msg.isSuperGroupMessage()) {
                if(update.hasMessage() && pht && msg.isUserMessage()){

                    // Know file_id
                    f_id = photos.stream()
                            .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                            .findFirst()
                            .orElse(null).getFileId();
//                    // Know photo width
//                    int f_width = photos.stream()
//                            .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
//                            .findFirst()
//                            .orElse(null).getWidth();
//                    // Know photo height
//                    int f_height = photos.stream()
//                            .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
//                            .findFirst()
//                            .orElse(null).getHeight();

                    giveawayMediaMap.put(id, f_id);
                    InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

                    List<InlineKeyboardButton> addRow = new ArrayList<>();

                    // Add buttons for adding photo or video
//                    InlineKeyboardButton confirmAddButton = new InlineKeyboardButton("Add Photo to Giveaway");
//                    confirmAddButton.setCallbackData("confirm_add_photo");
//                    addRow.add(confirmAddButton);

                    InlineKeyboardButton cancelButton = new InlineKeyboardButton("Cancel Upload");
                    cancelButton.setCallbackData("cancel_add");
                    addRow.add(cancelButton);

                    inlineButtons.add(addRow);
                    inlineKeyboardMarkup.setKeyboard(inlineButtons);

                    SendMessage sm = new SendMessage();
                    sm.setChatId(msg.getChatId());
                    sm.setText("Upload successful! Here is your giveaway with photo: ");

                        // Set photo caption
//                        String caption = "file_id: " + f_id + "\n" +
//                                "width: " + Integer.toString(f_width) + "\n" +
//                                "height: " + Integer.toString(f_height);
                        String caption = "Giveaways details:\n" +
                                "Title: " + giveawayTitleMap.get(id) + "\n" +
                                "Description: " + giveawayDescriptionMap.get(id) + "\n" +
                                "Date: " + giveawayDateMap.get(id) + "\n\n" +
                                "Winners Count: " + giveawayWinnersMap.get(id) + "\n" +
                                "Conditions: " + giveawayConditionMap.get(id) + "\n" ;
                        SendPhoto pmsg = new SendPhoto();
                        pmsg.setChatId(chatId);
                        pmsg.setPhoto(new InputFile(f_id));
                        pmsg.setCaption(caption);
                        pmsg.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            execute(sm);
                            execute(pmsg); // Call method to send the photo with caption
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    storeMedia(1L, f_id);
                }else if (update.hasMessage() && vid && msg.isUserMessage()) {

                    // Get the file ID of the video
                    f_id = video.getFileId();

//                    // Get the video width and height
//                    int width = video.getWidth();
//                    int height = video.getHeight();
//
//                    // Get the video duration (in seconds)
//                    int duration = video.getDuration();
                    InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

                    List<InlineKeyboardButton> addRow = new ArrayList<>();

                    // Add buttons for adding photo or video
//                    InlineKeyboardButton confirmAddButton = new InlineKeyboardButton("Add Video to Giveaway");
//                    confirmAddButton.setCallbackData("confirm_add_video");
//                    addRow.add(confirmAddButton);

                    InlineKeyboardButton cancelButton = new InlineKeyboardButton("Cancel Upload");
                    cancelButton.setCallbackData("cancel_add");
                    addRow.add(cancelButton);

                    inlineButtons.add(addRow);
                    inlineKeyboardMarkup.setKeyboard(inlineButtons);

                    // Send the video details back to the user
                    SendMessage sm = new SendMessage();
                    sm.setChatId(msg.getChatId());
                    sm.setText("Upload successful! Here is your giveaway with video:");

                    // Set the video caption with details
//                    String caption = "File ID: " + fileId + "\n";
//                    caption += "Width: " + width + "\n";
//                    caption += "Height: " + height + "\n";
//                    caption += "Duration: " + duration + " seconds\n";
                    String caption = "Giveaways details:\n" +
                            "Title: " + giveawayTitleMap.get(id) + "\n" +
                            "Description: " + giveawayDescriptionMap.get(id) + "\n" +
                            "Date: " + giveawayDateMap.get(id) + "\n\n" +
                            "Winners Count: " + giveawayWinnersMap.get(id) + "\n" +
                            "Conditions: " + giveawayConditionMap.get(id) + "\n" ;

                    SendVideo vmsg = new SendVideo();
                    vmsg.setChatId(chatId);
                    vmsg.setVideo(new InputFile(f_id));
                    vmsg.setCaption(caption);
                    vmsg.setReplyMarkup(inlineKeyboardMarkup);

                    try {
                        execute(sm);
                        execute(vmsg);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    storeMedia(1L, f_id);
                }


                switch (txt) {
                    case "/start@AH_Prizebot" :
                    case "/start" : {
                        System.out.println
                                ("AH prizebot started successfully!!");
                        SendPhoto sp = new SendPhoto();
                        sp.setChatId(chatId);
                        sp.setPhoto(new InputFile("AgACAgUAAxkBAAIS4WXb9QWyKvwXBNieAgXER1wL_zTHAAJpwDEb-6bZVikoGDgZ_ZIJAQADAgADdwADNAQ"));
                        sp.setCaption("Hello! I am AH Prizebot. You can openly raffle off prizes among random users here. " +
                                "The ability to set requirements for members (such as needing to be a subscriber on certain channels) is one of the primary features. \n" +
                                "Random.org powers the bot's random, and you may verify the sources there. " +
                                "\n Use /help to see all available commands. ");
                        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
                        List<KeyboardRow> keyboardRowList = new ArrayList<>();
                        KeyboardRow kbr = new KeyboardRow();
                        KeyboardRow kbr2 = new KeyboardRow();
                        KeyboardRow kbr3 = new KeyboardRow();
                        var kbb = new KeyboardButton("Help");
                        var kbb2 = new KeyboardButton("New Giveaway");
                        var kbb3 = new KeyboardButton("My Giveaways");
                        kbr.add(kbb);
                        kbr2.add(kbb2);
                        kbr3.add(kbb3);
                        keyboardRowList.add(kbr);
                        keyboardRowList.add(kbr2);
                        keyboardRowList.add(kbr3);
                        rkm.setKeyboard(keyboardRowList);
                        sp.setReplyMarkup(rkm);
                        try {
                            execute(sp);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "Help" :
                    case "/help" : {
                        System.out.println("Received /help command");
                        SendMessage sm = new SendMessage();
                        sm.setText("The available commands list:\n" +
                                "- /start: Starts me\n" +
                                "- /help: Sends this message\n" +
                                "- /giveaway: Create a new giveaway\n" +
                                "- /my_giveaways: Get the list of giveaways created\n" +
                                "- /language: Select bot's language");
//                sm.setParseMode(ParseMode.MARKDOWN);
                        sm.setChatId(msg.getChatId());
                        try {
                            execute(sm);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "New Giveaway" :
                    case "/giveaway" : {
                        System.out.println("Received /giveaway command");
                        SendMessage sm = new SendMessage();
                        sm.setText("Let's start creating a new raffle, first,send me its title (type /cancel to cancel)");
//                sm.setParseMode(ParseMode.MARKDOWN);
                        sm.setChatId(msg.getChatId());
                        try {
                            execute(sm);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        // Set the giveaway creation state to ENTER_TITLE

                        if (msg.isUserMessage()) {
                            giveawayCreationState.put(id, GiveawayStep.ENTER_TITLE);
                            // Set the default emoji for participation
                        }

                    }
                    break;
                    case "/cancel" : {
                        System.out.println("Received /cancel command");
                        // Clear the giveaway creation state and associated data
                        giveawayCreationState.remove(id);
                        giveawayTitleMap.remove(id);
                        giveawayEmojiMap.remove(id);
                        giveawayDateMap.remove(id);
                        SendMessage sm = new SendMessage();
                        sm.setText("Cancelled!");
                        sm.setChatId(msg.getChatId());
                        try {
                            execute(sm);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        // Clear the giveaway creation state
                        giveawayCreationState.remove(id);
                    }
                    break;
                    case "My Giveaways" :
                    case "/my_giveaways" : {
                        System.out.println("Received /my_giveaways command");

                        // Check if userGiveawaysMap contains key 1L and its value is not empty
                        if (userGiveawaysMap.containsKey(1L) && !userGiveawaysMap.get(1L).isEmpty()) {
                            // Your existing code to handle the case when there are giveaways stored

                            SendMessage sm = new SendMessage();
                            List<Map.Entry<Long, List<String>>> allData = getAllDataFromMap();
                            List<Map.Entry<Long, String>> allMedia = getMediaFromMap();

                            //String caption = "";
                            SendPhoto sp = new SendPhoto();
                            sp.setChatId(chatId);
                            for (Map.Entry<Long, String> m_entry : allMedia) {
                                String giveawayMedia = m_entry.getValue();
                                sp.setPhoto(new InputFile(giveawayMedia));
                            }

                            StringBuilder messageText = new StringBuilder("Your giveaways:\n");
                            int i = 1;
                            for (Map.Entry<Long, List<String>> entry : allData) {
                                String giveawayDetails = entry.getValue().get(0); // Assuming only one detail for simplicity
                                messageText.append(i).append("- ").append("\n");
                                messageText.append(giveawayDetails).append("\n\n");

                                InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                                List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

                                List<InlineKeyboardButton> emojiRow = new ArrayList<>();
                                InlineKeyboardButton joinButton = new InlineKeyboardButton();
                                //if (giveawayEmojiMap.isEmpty()){
                                    joinButton.setText(/*giveawayEmojiMap.get(id)*/"JOIN NOW");
                                //}
//                                else {
//                                    joinButton.setText(giveawayEmojiMap.get(id));
//                                }

                                joinButton.setCallbackData("join_giveaway");
                                emojiRow.add(joinButton);

                                inlineButtons.add(emojiRow);
                                inlineKeyboardMarkup.setKeyboard(inlineButtons);
                                sm.setReplyMarkup(inlineKeyboardMarkup);

                                i++;
                            }
                            sm.setText(messageText.toString());
                            //sp.setCaption(messageText.toString());
                            sm.setChatId(msg.getChatId());
                            if (!giveawayMediaMap.isEmpty()) {
                                try {
                                    execute(sp);
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }else{
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            // Your existing code to handle the case when there are no giveaways stored
                            SendMessage sm = new SendMessage();
                            sm.setText("You haven't created any giveaways yet.");
                            sm.setChatId(msg.getChatId());
                            try {
                                execute(sm);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                    break;
                    case "/raffle":{
                        // Call conductRaffle function to get the list of winners
                        long groupId = update.getMessage().getChat().getId();
                        conductRaffle(groupId);
                    }
                    break;
                    case "/language" : {
                        System.out.println("Received /language command");
                        SendMessage sm = new SendMessage();
                        sm.setText("Select the bot language here:");
                        sm.setParseMode(ParseMode.MARKDOWN);
                        sm.setChatId(msg.getChatId());

                        InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
                        List<InlineKeyboardButton> inlineKeyboardButtonList = new ArrayList<>();
                        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton("\uD83C\uDDEC\uD83C\uDDE7\uD83C\uDDFA\uD83C\uDDF8 English");
                        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton("\uD83C\uDDE8\uD83C\uDDF3 Chinese");
                        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton("\uD83C\uDDF2\uD83C\uDDFE Malay");
                        inlineKeyboardButton1.setCallbackData("english");
                        inlineKeyboardButton2.setCallbackData("chinese");
                        inlineKeyboardButton3.setCallbackData("malay");
                        inlineKeyboardButtonList.add(inlineKeyboardButton1);
                        inlineKeyboardButtonList.add(inlineKeyboardButton2);
                        inlineKeyboardButtonList.add(inlineKeyboardButton3);
                        inlineButtons.add(inlineKeyboardButtonList);
                        inlineKeyboardMarkup.setKeyboard(inlineButtons);
                        sm.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            execute(sm);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    default:{
                        GiveawayStep currentStep = giveawayCreationState.getOrDefault(id, GiveawayStep.NO_STATE);

                        switch (currentStep) {
                            case ENTER_TITLE: {
                                // Save the entered title and move to the next step
                                giveawayTitleMap.put(id, txt);
                                giveawayCreationState.put(id, GiveawayStep.ENTER_DESCRIPTION);

                                SendMessage sm = new SendMessage();
                                sm.setText("Please proceed to provide the details for your upcoming lucky draw, including the description and formal terms and conditions. ");
                                sm.setChatId(msg.getChatId());
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                            case ENTER_DESCRIPTION: {
                                giveawayDescriptionMap.put(id, txt);
                                giveawayCreationState.put(id, GiveawayStep.SETUP_PARTICIPATE_EMOJI);
                                SendMessage sm = new SendMessage();
                                sm.setText("Great! Now, please set up the text for participation.(Use /skip to use the default JOIN NOW)");
                                sm.setChatId(msg.getChatId());
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                            case SETUP_PARTICIPATE_EMOJI: {
                                // Save the entered emoji and move to the next step
                                if (txt.equals("/skip")){
                                    giveawayEmojiMap.put(id, "JOIN NOW");
                                }else {
                                    giveawayEmojiMap.put(id, txt);
                                }
                                giveawayCreationState.put(id, GiveawayStep.SETUP_CONDITION);

                                SendMessage sm = new SendMessage();
                                sm.setText("Now you can choose the conditions for the members of the giveaway. " +
                                        "(Use /next to create giveaway without conditions or /cancel to cancel)");
                                sm.setChatId(msg.getChatId());
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                            case SETUP_CONDITION: {
                                if (txt.equals("/next")) {
                                    giveawayConditionMap.put(id, "-");
                                } else {
                                    giveawayConditionMap.put(id, txt);
                                }
                                giveawayCreationState.put(id, GiveawayStep.SETUP_PARTICIPANTS);
                                SendMessage sm = new SendMessage();
                                sm.setText("How many participants are needed for the auto-raffle to take place? (Use /cancel to cancel)");
                                sm.setChatId(msg.getChatId());
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;

                            case SETUP_PARTICIPANTS: {
                                if (isValidNumber(txt)) {
                                    int participants = Integer.parseInt(txt);
                                    giveawayParticipantsMap.put(chatId, participants);
                                    giveawayCreationState.put(chatId, GiveawayStep.SETUP_WINNERS);
                                    SendMessage sm = new SendMessage();
                                    sm.setText("Enter the amount of winners for this giveaway. (Use /cancel to cancel)\n");
                                    sm.setChatId(msg.getChatId());
                                    try {
                                        execute(sm);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    SendMessage sm = new SendMessage();
                                    sm.setText("Invalid input. Please enter a valid number.");
                                    sm.setChatId(msg.getChatId());
                                    try {
                                        execute(sm);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            break;

                            case SETUP_WINNERS:
                                if (isValidNumber(txt)) {
                                    int winners = Integer.parseInt(txt);
                                    giveawayWinnersMap.put(chatId, winners);
                                    giveawayCreationState.put(chatId, GiveawayStep.SETUP_DATE);
                                    SendMessage sm = new SendMessage();
                                    sm.setText("Enter your auto-raffle date in this format: 00:00, 16.01.2024 (Time, Date). You may use /skip to skip or /cancel to cancel." + "\n" + "Timezone: GMT+8 Malaysia Time");
                                    sm.setChatId(msg.getChatId());
                                    try {
                                        execute(sm);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    SendMessage sm = new SendMessage();
                                    sm.setText("Invalid input. Please enter a valid number.");
                                    sm.setChatId(msg.getChatId());
                                }
                                break;

                            case SETUP_DATE: {
                                // Save the entered date and complete the giveaway creation
//                                giveawayDateMap.put(id, txt);
                                giveawayCreationState.remove(id); // Clear the state

                                String dateFormat = "HH:mm, dd.MM.yyyy";  // Specify the expected format
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                                SendMessage sm = new SendMessage();

                                if(txt.equals("/skip")){
                                    sm.setText("Your giveaway is created with the following details:\n" +
                                            "Title: " + giveawayTitleMap.get(id) + "\n" +
                                            "Description: " + giveawayDescriptionMap.get(id) + "\n" +
                                            "Winners Count: " + giveawayWinnersMap.get(id) + "\n" +
                                            "Conditions: " + giveawayConditionMap.get(id) + "\n" +
                                            "Raffle Now : /raffle");

                                    InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                                    List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
                                    List<InlineKeyboardButton> emojiRow = new ArrayList<>();
                                    InlineKeyboardButton joinButton = new InlineKeyboardButton(giveawayEmojiMap.get(id));
                                    joinButton.setCallbackData(giveawayEmojiMap.get(id));
                                    emojiRow.add(joinButton);
                                    joinButton.setCallbackData("join_giveaway");
                                    inlineButtons.add(emojiRow);

                                    // Second row for delete and raffle
                                    List<InlineKeyboardButton> actionRow = new ArrayList<>();
                                    InlineKeyboardButton deleteButton = new InlineKeyboardButton("Delete 🗑");
                                    InlineKeyboardButton raffleButton = new InlineKeyboardButton("Raffle \uD83C\uDF81");
                                    deleteButton.setCallbackData("delete");
                                    raffleButton.setCallbackData("raffle");
                                    actionRow.add(deleteButton);
                                    actionRow.add(raffleButton);
                                    inlineButtons.add(actionRow);

                                    // Third row for share
                                    List<InlineKeyboardButton> shareRow = new ArrayList<>();
                                    InlineKeyboardButton shareButton = new InlineKeyboardButton("Share \uD83D\uDCAC");
                                    shareButton.setCallbackData("share");
                                    shareRow.add(shareButton);
                                    inlineButtons.add(shareRow);

                                    int i = 1;
                                    List<InlineKeyboardButton> addRow = new ArrayList<>();

                                    // Add buttons for adding photo or video
                                    InlineKeyboardButton addPhotoButton = new InlineKeyboardButton("Add Photo");
                                    addPhotoButton.setCallbackData("add_photo_" + i);
                                    addRow.add(addPhotoButton);

                                    InlineKeyboardButton addVideoButton = new InlineKeyboardButton("Add Video");
                                    addVideoButton.setCallbackData("add_video_" + i);
                                    addRow.add(addVideoButton);
                                    inlineButtons.add(addRow);

                                    inlineKeyboardMarkup.setKeyboard(inlineButtons);
                                    sm.setReplyMarkup(inlineKeyboardMarkup);
                                }else if(txt.equals("/clear")){
                                    System.out.println("Received /cancel command");
                                    // Clear the giveaway creation state and associated data
                                    giveawayCreationState.remove(id);
                                    giveawayTitleMap.remove(id);
                                    giveawayEmojiMap.remove(id);
                                    giveawayDateMap.remove(id);
                                    sm.setText("Cancelled!");
                                    sm.setChatId(msg.getChatId());
                                    try {
                                        execute(sm);
                                    } catch (TelegramApiException e) {
                                        throw new RuntimeException(e);
                                    }
                                    // Clear the giveaway creation state
                                    giveawayCreationState.remove(id);
                                }else{
                                    try {
                                        LocalDateTime dateTime = LocalDateTime.parse(txt, formatter);

                                        // If the parsing is successful, update the giveaway date map
                                        giveawayDateMap.put(id, txt);

                                        // Perform any additional actions or inform the user that the giveaway is created

                                        sm.setText("Giveaways details:\n" +
                                                "Title: " + giveawayTitleMap.get(id) + "\n" +
                                                "Description: " + giveawayDescriptionMap.get(id) + "\n" +
                                                "Date: " + giveawayDateMap.get(id) + "\n\n" +
                                                "Winners Count: " + giveawayWinnersMap.get(id) + "\n" +
                                                "Conditions: " + giveawayConditionMap.get(id) + "\n" +
                                                "Raffle Now : /raffle");

                                        InlineKeyboardMarkup inlineKeyboardMarkup =  new InlineKeyboardMarkup();
                                        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
                                        List<InlineKeyboardButton> emojiRow = new ArrayList<>();
                                        InlineKeyboardButton joinButton = new InlineKeyboardButton(giveawayEmojiMap.get(id));
                                        joinButton.setCallbackData(giveawayEmojiMap.get(id));
                                        emojiRow.add(joinButton);
                                        joinButton.setCallbackData("join_giveaway");
                                        inlineButtons.add(emojiRow);

                                        // Second row for delete and raffle
                                        List<InlineKeyboardButton> actionRow = new ArrayList<>();
                                        InlineKeyboardButton deleteButton = new InlineKeyboardButton("Delete 🗑");
                                        InlineKeyboardButton raffleButton = new InlineKeyboardButton("Raffle \uD83C\uDF81");
                                        deleteButton.setCallbackData("delete");
                                        raffleButton.setCallbackData("raffle");
                                        actionRow.add(deleteButton);
                                        actionRow.add(raffleButton);
                                        inlineButtons.add(actionRow);

                                        // Third row for share
                                        List<InlineKeyboardButton> shareRow = new ArrayList<>();
                                        InlineKeyboardButton shareButton = new InlineKeyboardButton("Share \uD83D\uDCAC");
                                        shareButton.setCallbackData("share");
                                        shareRow.add(shareButton);
                                        inlineButtons.add(shareRow);

                                        int i = 1;
                                        List<InlineKeyboardButton> addRow = new ArrayList<>();

                                        // Add buttons for adding photo or video
                                        InlineKeyboardButton addPhotoButton = new InlineKeyboardButton("Add Photo");
                                        addPhotoButton.setCallbackData("add_photo_" + i);
                                        addRow.add(addPhotoButton);

                                        InlineKeyboardButton addVideoButton = new InlineKeyboardButton("Add Video");
                                        addVideoButton.setCallbackData("add_video_" + i);
                                        addRow.add(addVideoButton);
                                        inlineButtons.add(addRow);

                                        inlineKeyboardMarkup.setKeyboard(inlineButtons);
                                        sm.setReplyMarkup(inlineKeyboardMarkup);

                                    } catch (Exception e) {
                                        // If parsing fails, inform the user and prompt them to try again
                                        sm.setText("Please enter the date in the following format: " + dateFormat);
                                        giveawayCreationState.put(id, GiveawayStep.SETUP_DATE);
                                    }
                                }


                                storeText(1L, sm.getText());
                                sm.setChatId(msg.getChatId());



                                //Handle the share button callback
//                                if (update.hasCallbackQuery()) {
//                                    CallbackQuery callbackQuery = update.getCallbackQuery();
//                                    String callbackData = callbackQuery.getData();
//                                    if (callbackData.equals("share")) {
//                                        // Replace CHANNEL_USERNAME with the actual username or chat ID of your channel
//
//                                        Long targetChatId = -1001954778447L; // Replace with the actual chat ID
//                                        Integer messageIdToForward = msg.getMessageId();
//
//                                        ForwardMessage forwardMessage = new ForwardMessage();
//                                        //forwardMessage.setChatId(targetChatId);
//                                        forwardMessage.setChatId(msg.getForwardFromMessageId().longValue());
//                                        forwardMessage.setFromChatId(update.getMessage().getChat().getId()); // Replace with the actual source chat ID
//                                        forwardMessage.setMessageId(messageIdToForward);
//                                        // Forward the message to the channel
//                                        try {
//                                            // Execute the forward operation
//                                            Message forwardedMessage = execute(forwardMessage);
//
//                                            // Handle the forwarded message as needed
//                                            System.out.println("Message forwarded successfully. Forwarded message ID: " + forwardedMessage.getMessageId());
//                                        } catch (TelegramApiException e) {
//                                            // Handle any exceptions
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }
                                try {
                                    execute(sm);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                            break;

                            default: {
                                // Handle other cases or perform default actions
                            }
                        }
                    }
                }

            }
        }

    }

    private boolean isValidNumber(String txt) {
        try {
            int number = Integer.parseInt(txt);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SendMessage sendDefaultMessage(Update update) {
        String markdownMessage = "Default message here";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(markdownMessage.toString());
        return sendMessage;
    }

//    private void forwardGiveawayMessage(Update update) {
//        // Extract the chat ID of the giveaway message to forward
//        long chatIdToForward = update.getMessage().getChatId();
//        int messageIdToForward = update.getMessage().getMessageId();
//
//        // Replace CHANNEL_USERNAME with the actual username or chat ID of your channel
//        // You can also use a specific chat ID instead of the username
//        var channel_username = update.getMessage();
////        String targetChannelUsername = "CHANNEL_USERNAME";
//        long targetChannelUsername = -1001954778447L;
//
//        // Create a ForwardMessage object
//        ForwardMessage forwardMessage = new ForwardMessage();
//        forwardMessage.setChatId(targetChannelUsername);
//        forwardMessage.setFromChatId(chatIdToForward);
//        forwardMessage.setMessageId(messageIdToForward);
//
//        try {
//            // Execute the forward operation
//            execute(forwardMessage);
//            // Handle the forwarded message as needed
//            System.out.println("Message forwarded successfully. Forwarded message ID: " + messageIdToForward);
//        } catch (TelegramApiException e) {
//            // Handle any exceptions
//            e.printStackTrace();
//        }
//    }

    // Method to randomly select a subscribed user in a Telegram channel
    private void conductRaffle(long groupId) {
        // Retrieve the number of participants
        int participants = giveawayParticipantsMap.getOrDefault(groupId, 1);

         //Check if there are sufficient participants for the raffle
        if (participants <= 0) {
            // Handle the case when there are no participants or invalid number of participants
            SendMessage message = new SendMessage();
            message.setChatId(groupId);
            //message.setText("There are no participants for the raffle or the number of participants is invalid.");
            message.setText("There are no regular group members eligible for the raffle. Please ensure that there are non-administrator members in the group to participate.");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return; // Exit the method
        }

        // Retrieve the number of winners
        int winnersCount = giveawayWinnersMap.getOrDefault(groupId, 1); // Default to 1 winner if not specified

        // Create a list to store the usernames of the winners
        List<String> winners = new ArrayList<>();

        List<ChatMember> eligibleMembers = getEligibleMembers(groupId);
        if (eligibleMembers.isEmpty()) {
            // Handle the case when there are no eligible members
            SendMessage message = new SendMessage();
            message.setChatId(groupId);
            message.setText("There are no eligible members for the raffle.");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return; // Exit the method
        }

        // Generate a random index within the range of eligible members
        SendMessage message = new SendMessage();
        Random random = new Random();
        int randomIndex = random.nextInt(eligibleMembers.size());

        String winner = giveawayMemberList.get(randomIndex);
        int raffleCount = 0;

        // Remove the selected winner from the giveawayMemberList
        giveawayMemberList.remove(randomIndex);

        // Notify the selected winner
        message.setChatId(groupId);
        if(!giveawayMemberList.isEmpty() && giveawayMemberList.size() >1){
            raffleCount++;
            message.setText ("\uD83C\uDF89 Congratulations to " + winner + " for winning the raffle!\n"
                    + raffleCount+"/"+winnersCount);
        }else {
            message.setText("That is enough participants!");
        }

        // Retrieve the username of the member at the random index
//        ChatMember randomMember = eligibleMembers.get(randomIndex);
//        String winnerUsername = randomMember.getUser().getUserName();
//        String winnerFirstname = randomMember.getUser().getFirstName();
//        String winnerLastname = randomMember.getUser().getLastName();
//        winners.add(winnerUsername);
//        eligibleMembers.remove(randomIndex); // Remove the selected winner from eligible members
//        //message.setText(eligibleMembers.toString());
//        // Notify the selected user
//        // Notify the selected users
//        StringBuilder messageText = new StringBuilder("\uD83C\uDF89 Congratulations to these "+ winnersCount +" winner(s):\n" + eligibleMembers.size());
//        for (String winner : winners) {
//            messageText.append(winnerFirstname + " " + winnerLastname+" - ").append("@").append(winner).append("\n");
//        }
//
//        message.setChatId(groupId);
//        message.setText(messageText.toString());
//        SendMessage message = new SendMessage();
//        message.setChatId(groupId);
//        message.setText("\uD83C\uDF89 Congratulations to @" + winnerUsername + " for winning the raffle!");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private List<ChatMember> getEligibleMembers(long groupId) {
        List<ChatMember> eligibleMembers = new ArrayList<>();
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(groupId);
        List<ChatMember> chatAdministrators = new ArrayList<>();
        try {
            chatAdministrators = execute(getChatAdministrators);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        for (ChatMember member : chatAdministrators) {
            if (!member.getUser().getIsBot() && !member.getStatus().equals("creator")) {
                eligibleMembers.add(member);
            }
        }
        return eligibleMembers;
    }

}
