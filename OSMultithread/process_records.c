#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include "report_record_formats.h"
#include "queue_ids.h"
#include <pthread.h>
#include <signal.h>
#include <unistd.h>

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t print = PTHREAD_COND_INITIALIZER; //Lock and Cond var for threading
int freeToPrint = 0, totalReports, recordsRead = 0, *numMatches; // SIGINT REPORT

void sig_handler(int signo){ //handels Sigint
    pthread_mutex_lock(&lock);
    if(signo == SIGINT)
        pthread_cond_signal(&print);
    pthread_mutex_unlock(&lock);
}

void *printRecords(void *arg){
    while(freeToPrint != 2){
        pthread_mutex_lock(&lock);
        pthread_cond_wait(&print, &lock);
        pthread_mutex_unlock(&lock);
        fprintf(stdout, "***Report***\n%d records read for %d reports\n", recordsRead, totalReports);
        for(int i = 0; i < totalReports; i++)
            fprintf(stdout, "Records sent for report index %d: %d\n",i+1, numMatches[i]);
    }
}

void *findRecords(){
    int msqid;
    int msgflg = IPC_CREAT | 0666;
    key_t key;

    report_request_buf rbuf;
    report_record_buf sbuf;
    size_t buf_length;


    key = ftok(FILE_IN_HOME_DIR,QUEUE_NUMBER);
    if (key == 0xffffffff) {
        fprintf(stderr,"Key cannot be 0xffffffff..fix queue_ids.h to link to existing file\n");
        exit(1);
    }


    if ((msqid = msgget(key, msgflg)) < 0) {
        int errnum = errno;
        fprintf(stderr, "Value of errno: %d\n", errno);
        perror("(msgget)");
        fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
    }
    else
        fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);

    // msgrcv to receive report request
    int ret;
    do {
      ret = msgrcv(msqid, &rbuf, sizeof(rbuf), 1, 0);//receive type 1 message
      int errnum = errno;
      if (ret < 0 && errno !=EINTR){
        fprintf(stderr, "Value of errno: %d\n", errno);
        perror("Error printed by perror");
        fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
      }
    } while ((ret < 0 ) && (errno == 4));
    fprintf(stderr,"process-msgrcv-request: msg type-%ld, Record %d of %d: %s ret/bytes rcv'd=%d\n", rbuf.mtype, rbuf.report_idx,rbuf.report_count,rbuf.search_string, ret);
    //fprintf(stderr,"msgrcv error return code --%d:$d--",ret,errno);
    totalReports = rbuf.report_count;//Needed for actually getting all records
    numMatches = malloc(sizeof(int)*totalReports); //Used for status report
    char searchStrings[totalReports][SEARCH_STRING_FIELD_LENGTH]; // Array of searchStrings
    strcpy(searchStrings[0],rbuf.search_string);
    for(int i = 1; i < totalReports; i++){
        do {
            ret = msgrcv(msqid, &rbuf, sizeof(rbuf), 1, 0);//receive type 1 message
            int errnum = errno;
            if (ret < 0 && errno !=EINTR){
                fprintf(stderr, "Value of errno: %d\n", errno);
                perror("Error printed by perror");
                fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
            }
            fprintf(stderr,"process-msgrcv-request: msg type-%ld, Record %d of %d: %s ret/bytes rcv'd=%d\n", rbuf.mtype, rbuf.report_idx,rbuf.report_count,rbuf.search_string, ret);
        } while ((ret < 0 ) && (errno == 4));
        strcpy(searchStrings[i],rbuf.search_string);
    }
    pthread_mutex_lock(&lock); // Allows printing of status once all search strings in
    freeToPrint = 1;
    pthread_cond_signal(&print);
    pthread_mutex_unlock(&lock);

    char line[RECORD_FIELD_LENGTH+1]; // For newline character if at 80
    while(!feof(stdin)){ // Comparing Search Strings to reports
        fgets(line, sizeof(line),stdin);
        for(int i = 0; i < totalReports; i++){
            if(strstr(line, searchStrings[i]) != NULL){
                pthread_mutex_lock(&lock);
                numMatches[i] ++;
                pthread_mutex_unlock(&lock);

                key = ftok(FILE_IN_HOME_DIR,i+1);
                if (key == 0xffffffff) {
                    fprintf(stderr,"Key cannot be 0xffffffff..fix queue_ids.h to link to existing file\n");
                    exit(1);
                }
                if ((msqid = msgget(key, msgflg)) < 0) {
                    int errnum = errno;
                    fprintf(stderr, "Value of errno: %d\n", errno);
                    perror("(msgget)");
                    fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
                }
                else
                    fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);
                sbuf.mtype = 2;
                strcpy(sbuf.record,line);
                buf_length = strlen(sbuf.record) + sizeof(int)+1;
                if((msgsnd(msqid, &sbuf, buf_length, IPC_NOWAIT)) < 0) {
                    int errnum = errno;
                    fprintf(stderr,"%d, %ld, %s %d\n", msqid, sbuf.mtype, sbuf.record, (int)buf_length);
                    perror("(msgsnd)");
                    fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
                    exit(1);
                }
                else
                fprintf(stderr,"msgsnd-report_record: record\"%s\" Sent (%d bytes)\n", sbuf.record,(int)buf_length);
            }
            recordsRead ++; //Sleeping
            if(recordsRead == 10)
                sleep(5);
        }
    }
    fprintf(stderr, "0 length records:\n");
    for(int i = 0; i < totalReports; i++){
        key = ftok(FILE_IN_HOME_DIR,i+1);
        if (key == 0xffffffff) {
            fprintf(stderr,"Key cannot be 0xffffffff..fix queue_ids.h to link to existing file\n");
            exit(1);
        }
        if ((msqid = msgget(key, msgflg)) < 0) {
            int errnum = errno;
            fprintf(stderr, "Value of errno: %d\n", errno);
            perror("(msgget)");
            fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
        }
        else
            fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);
        sbuf.mtype = 2;
        sbuf.record[0]=0;
        buf_length = strlen(sbuf.record) + sizeof(int)+1;//struct size without
        // Send a message.
        if((msgsnd(msqid, &sbuf, buf_length, IPC_NOWAIT)) < 0) {
            int errnum = errno;
            fprintf(stderr,"%d, %ld, %s, %d\n", msqid, sbuf.mtype, sbuf.record, (int)buf_length);
            perror("(msgsnd)");
            fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
            exit(1);
        }
        else
            fprintf(stderr,"msgsnd-report_record: record\"%s\" Sent (%d bytes)\n", sbuf.record,(int)buf_length);
    }

    pthread_mutex_lock(&lock);
    freeToPrint = 2;
    pthread_cond_signal(&print);
    pthread_mutex_unlock(&lock);
}




int main(int argc, char**argv)
{
    if(signal(SIGINT, sig_handler) == SIG_ERR)
        fprintf(stderr, "Error with SIGINT");
    pthread_t mainReport;
    pthread_t printStatus;
    pthread_create(&mainReport,NULL,findRecords,NULL);
    pthread_mutex_lock(&lock);
    while(freeToPrint == 0)
        pthread_cond_wait(&print, &lock);
    pthread_mutex_unlock(&lock);
    pthread_create(&printStatus, NULL, printRecords, NULL);

    pthread_join(mainReport, NULL);
    pthread_join(printStatus, NULL);
    if(numMatches != NULL)
        free(numMatches); 

    exit(0);
}
