
// exemple de Mr.Guyot prof
// C'est le main ici --> On instancie l'obj et on lui passse le paramètre c'est tout (ici c'est en locale en ligne de cmd) 
// Mais ds le main y'a rien
// IL faut un booleen logReplay et un boolen logRecording (on part de log.txt pr les 2). On ne peut pas etre en meme temps en enregistrement et en replay. Le prof fera le replay après le recording et ne testera pas le blindage
import src.PileRPL;
import src.ObjEmp;

// Pour le socket en mode mode
import java.net.*;
// Pour Lire le Buffer
//import java.io.BufferedInputStream;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.*;

// Buffer Reader en 
public class CalcUI { // C'est le user qui va manipuler cet obj
// Les args passé vont permettre de récupérer et d'initialiser les flux utilisateurs avec cles args qu'on récupère (ON A QUE LES FLUX d'ENTREES LOCALE OU DISTANT)
// Dans un premiers temps on ne veut que un utilisateur de type unique
    //Exemple de main du prof
    //public static void main(String [] args){ // attr inputUser, OutpUser; loop
        //CaclUI ui = new CaclUI(args);
    //}
    private PileRPL pile;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader inputUser;
    private BufferedReader inputReplay; // Revu des actions du fichier de log
    private BufferedReader inputLog; // Buffer d'un fichier de log à lire en flux d'entrée pour activer lemode replay.
    private PrintStream outputUser;
    private PrintStream outputLog; // log recording pour savoir qd on enregistre maispas la var ici nécessairement OU REVU de session ne serait-ce que pour avoir l'écho de ce qu'on interprète.
    private boolean logMode = false;
    private boolean replayMode = false;
    private boolean exit = false;
    
    // On peut initialiser la pile au début dans son constructeur comme ca on a déjà son argument
    public CalcUI(String [] args) {
        try{
            initStreams(args); 
            // Initialisation des fluxs (y'en a plusieurs)
            // Boucle for à mettre plutoit dans initStreams d'après le prof (à voir pourquoi)
            mainloop();
        }
            
        catch (Exception e){
            System.out.println("erreur");
        }
            
    }

    // Une sorte de faux main donc c'est pas static
    public void mainloop(){
            while(true){ // while(loop) plus tard, est un booléen qui change en f° des args
                try {
                    // On déclare une pile ici
                    outputUser.println(pile); // Outstream textuel bufferisée pour afficher la pile ou l'état de la pile etc... ça se fait avec la méthode toString redéfinit dans la classe PileRPL
                    String cmd = inputUser.readLine(); // Lecture inputStream du User (pour le moment ce n'est que fait en local) (ajouter l'aide avec le help) - ou d'un fichier
                    if (logMode == true) {
                        // Si en mode "recording", enregistrez les commandes dans le fichier de log
                        outputLog.println(cmd);
                        outputLog.flush();
                    }
                    //System.out.println("écriture: " + cmd); // pas besoin de cette commande mais permet de voir qu'on récupère bien les commandes du flux d'entrée
                    if (cmd.equals("exit")) return; // Pour quitter leprog sans faire un System.exit(0); qui serait trop brutal
                    cmdParser(cmd); //--> Revoir comment faire et définir les différentes commande possible du user (par exemple push, pop, exit et puis les opérateur comme mult, add, div, sub)
                    /// CEST ICI QU'on définit le switch pour la pile en fonction des commandes push etc.
                    // if ("push")

                }
                catch (Exception e){
                    if (!e.getMessage().equals("java.lang.NullPointerException: Cannot invoke \"src.PileRPL.push(src.ObjEmp)\" because \"this.pile\" is null"))
                        System.out.println(e); // A changer
                    // Si l'erreur est que c'est la fin de la lecture du fichier
                    else {
                        // !!! Ce CODE NE MARCHE PAS CA QUITTE SOIT directe quand y'a un exit en fin de fichier 
                        /// !! Et quand ya pas de exit en fin de fichier --> boucle infini avec null pointer sur la pile 
                        try{
                            initFullLocal(logMode);
                        }
                        catch (Exception excep){
                            System.out.println(excep);
                        }
                    }
                }
            }
    }

// FileReader = flux textuel d'entrée bufferisé (revoir avec des vidéos) alors que y'a d'autres types de flux qui sont par caractères ou par bytes.
    public void initStreams(String [] args){
        System.out.println("initStreams\n");
        //String response = inputUser.readLine(); A décommenter !
        // les outputs vont etre initialisé dans chacune des f° ci-dessous
        String argument = args[0];// argument user:local/remote
        String pileSize = args[1]; //3
        String recordOrReplay = ""; // rec/rep=./chemin/vers/ficher après user:local pour indiquer le mode recording

        if (args.length == 3) recordOrReplay = args[2]; // le 2eme arg peut-être soit record soit replay
        try{
                if (argument.equals("user:local") && (recordOrReplay.equals("")) ) {
                pile = new PileRPL(Integer.valueOf(pileSize)); //Init de la pile
                initFullLocal(logMode);
                // Connecter l'utilisateur en mode "log".
                System.out.println("init flux local sans option\n");
            } else if ((argument.equals("user:local")) && (recordOrReplay.contains("rep="))) {
                pile = new PileRPL(Integer.valueOf(pileSize)); //Init de la pile
                String filepath = "";
                // Connecter l'utilisateur en mode "replay".
                replayMode = true;
                filepath = recordOrReplay.split("=")[1];
                System.out.println(filepath);
                initFullReplayLocale(filepath);
                System.out.println("init flux replay\n");
            } else if ((argument.equals("user:local")) && (recordOrReplay.contains("rec="))) {
                System.out.println("USER:LOCAL");
                // Connecter l'utilisateur en mode "local" | OK
                pile = new PileRPL(Integer.valueOf(pileSize));
                // Si user:local:log dans l'argument --> On passe le bool à 'true'
                /// !!! ATTENTION !!! C'est surement la mauvaise variable avant le equals/contains
                if (recordOrReplay.equals("-rec")) logMode = true;
                initFullLocal(logMode);
            } else if (argument.equals("user:remote")) {
                // Connecter l'utilisateur en mode "local".
                System.out.println("init flux remotes\n");
                pile = new PileRPL(Integer.valueOf(pileSize));
                initFullRemote();
            }  
            else {
                System.out.println("Argument non reconnu : " + argument);
            }
        }catch (Exception e){
            System.out.println(e); // Pour le mmt on le fait en out.println
        }
        
        
        /*for (int i = 0; i < args.length; i++)
            System.out.println(args[i]);*/ //--> A voir cmt implémenter un for pour lire chaque arg
        
        //initFullLocal();              A CODER
        //initFullRemote();             A CODER
        //initFullReplayLocale();       A CODER
        //initReplayNetwork();          A CODER
    }

    // Initialisation du initFullLocal avec 
    public void initFullLocal(boolean logMode) throws Exception {
        inputUser = new BufferedReader(new InputStreamReader(System.in));
        outputUser = System.out;
        //Si c'est en mode log on ouvre un fichier et les flux sortants seront redirigés dedans
        if (logMode){
            outputLog = new PrintStream(new FileOutputStream("command_log.txt", true), true, "UTF-8");
        }
    }
    public void initFullRemote() throws Exception{
        serverSocket = new ServerSocket(12345); // Utilisez un numéro de port approprié
        System.out.println("En attente de connexions distantes...");
        clientSocket = serverSocket.accept();
        System.out.println("Client connecté.");
        inputUser = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputUser = new PrintStream(clientSocket.getOutputStream());
    }

    public void initFullReplayLocale(String filename){
        try {
			inputUser = new BufferedReader(new FileReader(filename));
            System.out.println(filename);
            outputUser = System.out;
		} catch( FileNotFoundException exc ) {
			System.out.println( "le fichier n'existe pas" );
			System.exit( 1 );
		}
    }
    public void initReplayNetwork(){
        
    }

    public void cmdParser(String cmd){
        if (cmd.contains("push")){
            String argsInCommand = cmd.split(" ", 2)[1]; //2 pour séparer just en 2 morceau le tableau
            System.out.println("command: push");
            System.out.println("les args: " + argsInCommand);
            //String [] separatedArgsInCommand = cmd.split(" ");
            System.out.println("ObjEmp crée");
            //ObjEmp newElem = new ObjEmp(Integer.valueOf(separatedArgsInCommand[0]), Integer.valueOf(separatedArgsInCommand[1]));
            ObjEmp newElem = ObjEmp.parseInput(argsInCommand);
            pile.push(newElem);
            System.out.println("ObjEmp Ajouté à la pile");
        }
        else if (cmd.contains("pop")){
            pile.pop();
        }
        else if (cmd.contains("add")){
            pile.add();
        }
        else if (cmd.contains("sub")){
            pile.substract();
        }
        else if (cmd.contains("mult")){
            pile.multiply();
        }
        else if (cmd.contains("div")){
            pile.divide();
        }
        // Faire une fonction pour voir comment bien quitter (remote => fermer socket ou local => pas juste exite en pure et dure.)
        else if (cmd.contains("exit")){
            System.out.println("command: exit");

        }
        else System.out.println("regarder l'aide avec l'option -h");
    }
}

// Regarder la partier 4 du cours de java pour faire des multi user sur le serveur e,n remote