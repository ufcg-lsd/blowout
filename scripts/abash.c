#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

int main(int argc, char **argv) {
  int i;
  char *buffer;

  buffer = malloc(1024);

  buffer[0] = '\0';
 
  if (argc < 4) exit(0);

  for (i = 3; i < argc; i++) sprintf(buffer, "%s %s", buffer, argv[i]);

  printf("%s\n", buffer);

  int suid = atoi(argv[1]);
  setuid(suid);
  int sgid = atoi(argv[2]);
  setgid(sgid);
  int status = system(buffer);

}

