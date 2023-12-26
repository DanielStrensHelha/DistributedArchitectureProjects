#include "chatapp.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>
#include <unistd.h>
#include <ncurses.h>

typedef struct {
    CLIENT* client;
    char* host;
    char* username;
} Config;

Config config;
WINDOW *mainWin, *inputWin, *chatWin;
pthread_t thread;

void sigint_rsi() {
    #ifndef	DEBUG
        clnt_destroy(config.client);
    #endif	 /* DEBUG */
    pthread_cancel(thread);

    wclear(inputWin);
    wclear(chatWin);
    wclear(mainWin);

    delwin(inputWin);
    delwin(chatWin);
    delwin(mainWin);

    endwin();
    exit(EXIT_SUCCESS);
}

void initPantalles() {
    mainWin = initscr();
    noecho();
    cbreak();
    curs_set(0);

    // Init chat window
    chatWin = subwin(mainWin, LINES*0.3, COLS - 2, 1, 1);
    wmove(chatWin, 0, 0);
    wprintw(chatWin, "....... Welcome %s!", config.username);
    wrefresh(chatWin);
    scrollok(chatWin, TRUE);

    // Init input box
    inputWin = subwin(mainWin, 3, COLS - 2, (LINES * 0.3) + 1, 1);
}


void chatapp_1() {
    CLIENT *clnt;

    #ifndef	DEBUG
        clnt = clnt_create (config.host, CHATAPP, VER1, "udp");
        if (clnt == NULL) {
            clnt_pcreateerror (config.host);
            raise(SIGINT);
        }
    #endif	/* DEBUG */

    config.client = clnt;
}

void sendMsg(char* message) {
    int* result;
    message send_message_1_arg;

    send_message_1_arg.sender = config.username;
    send_message_1_arg.content = message;

    result = send_message_1(&send_message_1_arg, config.client);
    if (result == (int*)NULL){
        clnt_perror (config.client, "send_message call failed");
        raise(SIGINT);
    }

    free(message);
}

void receiveChat() {
    chat_history* result;

    result = get_chat_history_1(NULL, config.client);
    if (result == (chat_history*)NULL){
        clnt_perror (config.client, "get_chat_history call failed");
        raise(SIGINT);
    }
    wmove(chatWin, 2, 0);
    for (int i = 0; i < result->count; i++) {
        wprintw(chatWin, "%s: %s\n", result->messages[i].sender, result->messages[i].content);
    }
    wrefresh(chatWin);
}

void* listenChat(void) {
    while(1) {
        receiveChat();
        usleep(100000);
    }
}

int main (int argc, char *argv[]) {

    if (argc != 3) {
        printf ("usage: %s server_host username\n", argv[0]);
        exit (1);
    }

    signal(SIGINT, sigint_rsi);

    config.host = argv[1];
    config.username = argv[2];

    // Configure client
    chatapp_1();

    initPantalles();

    // Thread that updates chat
    pthread_create(&thread, NULL, (void*)listenChat, NULL);

    char* line = NULL;
    while (1) {
        wclear(inputWin);
        wmove(inputWin, 0, 0);
        wprintw(inputWin, "%s --> ", config.username);
        wrefresh(inputWin);
        echo();
        wscanw(inputWin, "%m[^\n]", &line);
        noecho();
        sendMsg(line);
    }
    
    // Will never reach here
    exit (0);
}
