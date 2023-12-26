struct message {
  string sender<100>;
  string content<500>;
};

struct chat_history {
  message messages<1000>;
  int count;
};

program CHATAPP {
  version VER1 {
    chat_history get_chat_history(void) = 1;
    int send_message(message) = 2;
  } = 1;
} = 0x3a3afeeb;