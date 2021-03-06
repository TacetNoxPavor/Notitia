package Commands.General;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScpCommand extends Command {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScpCommand.class);

    public ScpCommand()
    {
        this.name = "scp";
        this.help = "Load the details of a provided SCP";
        this.arguments = "<SCP Number 2-3000 or so>";
        this.guildOnly = false;
        this.category = new Category("User Commands");

    }

    /**
     * The main body method of a {@link Command Command}.
     * <br>This is the "response" for a successful
     * {@link Command#run(CommandEvent) #run(CommandEvent)}.
     *
     * @param event The {@link CommandEvent CommandEvent} that
     *              triggered this Command
     */
    @Override
    protected void execute(CommandEvent event) {

        log.info("SCP Called");
        if (event.getArgs().isEmpty()){
            event.replyError("You need to provide me an SCP number to load!");
        }else{

            String[] items = event.getArgs().split("\\s+");
            int number = Integer.valueOf(items[0]);
            HttpURLConnection conn = null;
            String SCPNum = "";
            String SCPClass = "";
            String Containment = "";
            String Description = "";
            String mainURL = "http://www.scp-wiki.net/scp-";
            if(number<100){
                mainURL = "http://www.scp-wiki.net/scp-0";
                if(number<10){
                   mainURL = "http://www.scp-wiki.net/scp-00";
                }
            }

            try{
                URL url = new URL(mainURL+number);
                conn=(HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inLine;
                int keepGoing = 0;
                while((inLine=in.readLine())!=null){
                    //System.out.println(inLine+"\n");
                    if(inLine.startsWith("<p><strong>Item #:</strong>")){
                        String temp = inLine.substring(27,inLine.length()-4);
                        //System.out.println("\n\n"+temp);
                        SCPNum = temp;
                        //System.out.println(inLine);
                    }
                    if(inLine.startsWith("<p><strong>Object Class:</strong>")){
                        String temp = inLine.substring(33, inLine.length()-4);
                        //System.out.println("\n\n"+temp);
                        SCPClass = temp;
                    }
                    if(inLine.startsWith("<p><strong>Special Containment Procedures:</strong>")){
                        String temp = inLine.substring(51, inLine.length()-4);
                        //System.out.println("\n\n"+temp);
                        Containment = temp;
                    }
                    if((keepGoing==1) && inLine.startsWith("<p>")){
                        if(inLine.startsWith("<p><strong>")){
                            keepGoing =0;
                        }else{
                            Description = Description.concat(inLine);
                        }
                    }

                    if(inLine.startsWith("<p><strong>Description:</strong>")) {
                        String temp = inLine.substring(32, inLine.length() - 4);
                        //System.out.println("\n\n"+temp);
                        Description = temp;
                        keepGoing = 1;
                    }


                }

                String fixedCon = Jsoup.parse(Containment).text();
                String fixedDesc = Jsoup.parse(Description).text();

                //System.out.println(SCPNum);
                //System.out.println(SCPClass);
                //System.out.println("\n\n"+fixedCon);
                //System.out.println("\n\n"+fixedDesc);
                in.close();
                if(fixedCon.length()>1023)
                    fixedCon = fixedCon.substring(0, 1000) + " (Continued in link...)";

                if(fixedDesc.length()>1023)
                    fixedDesc = fixedDesc.substring(0,1000) + " (Continued in link...)";


                Message message;

                EmbedBuilder embed = new EmbedBuilder();
                MessageEmbed test;
                embed
                        .addField("SCP #: ", event.getArgs(), true)
                        .addField("SCP Class: ", SCPClass, true)
                        .addField("Containment Procedures: ", fixedCon, false)
                        .addField("Description: ", fixedDesc, false)
                        .addField("Link to full page: ", mainURL+number, false);
                if(SCPClass.trim().equalsIgnoreCase("safe")){
                    embed.setColor(Color.GREEN);
                }else if(SCPClass.trim().equalsIgnoreCase("euclid")){
                    embed.setColor(Color.BLUE);
                }else if(SCPClass.trim().equalsIgnoreCase("keter")){
                    embed.setColor(Color.RED);
                }else{
                    embed.setColor(Color.PINK);
                }
                test = embed.build();

                MessageBuilder last = new MessageBuilder();
                message = last.setEmbed(test).build();

                //Send it off
                event.reply(message);

            }catch (Exception e){
                e.printStackTrace();
            }finally{
                conn.disconnect();
            }

        }


    }
}