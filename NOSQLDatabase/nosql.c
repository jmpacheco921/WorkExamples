#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <strings.h>
#include <limits.h>

typedef struct {
  int *array;
  size_t used;
  size_t size;
} Array;

void initArray(Array *a, size_t initialSize) {
  a->array = malloc(initialSize *sizeof(int));
  a->used = 0;
  a->size = initialSize;
}

void insertArray(Array *a, int element) {
  // a->used is the number of used entries, because a->array[a->used++] updates a->used only *after* the array has been accessed.
  // Therefore a->used can go up to a->size 
  if (a->used == a->size) {
    a->size *= 2;
    a->array = realloc(a->array, a->size * sizeof(int));
  }
  a->array[a->used++] = element;
}

void freeArray(Array *a) {
  free(a->array);
  a->array = NULL;
  a->used = a->size = 0;
}


typedef struct Document
{
    int id, security;
    Array fields;
    Array fieldNames;
    struct Document *next;
}Document;

Document *head = NULL;
Document *current = NULL;

void find(FILE *fp, int security){
    Array searchFields;
    Array searchVals;
    Array operators;
    Array projections;
    initArray(&searchFields,5);
    initArray(&searchVals,5);
    initArray(&operators,5);
    initArray(&projections,5);
    char condition[30] = "";
    char queryValue[10] = "";
    char temp[3];
    fgets(condition, 30, fp);
    while(strstr(condition, ";") == NULL && !feof(fp)){
        for(int i = 0; i < strlen(condition); i++){
            if(isalpha(condition[i]) != 0)
                insertArray(&searchFields, condition[i]);
            else if(condition[i] == '=')
                insertArray(&operators, 1);
            else if(condition[i] == '<')
                insertArray(&operators, 2);
            else if(condition[i] == '>')
                insertArray(&operators,3);
            if(isdigit(condition[i]) != 0){
                sprintf(temp, "%c", condition[i]);
                strncat(queryValue, temp,1);
            }else if(condition[i] == '\n')
                insertArray(&searchVals, atoi(queryValue));
        }
        strcpy(queryValue, "");
        fgets(condition, 100, fp);
    }
    // fgets(condition, 100, fp);
    for(int i = 0; i < strlen(condition); i++){
        if(isalpha(condition[i]) != 0)
            insertArray(&projections, condition[i]);
    }

    Document *findDoc = head;
    int matchQuery;
    int fieldExists;
    int fieldFound;
    while(findDoc != NULL){
        matchQuery = 1;
        fieldExists = 1;
        for(int i = 0; i < searchFields.used; i++){
            fieldFound = 0;
            if(matchQuery == 0 || fieldExists == 0)
                break;
            for(int j = 0; j < findDoc->fields.used; j++){
                if(findDoc->security > security){
                    matchQuery = 0;
                    break;
                }
                if(searchFields.array[0] == 'Z'){
                    fieldFound = 1;
                    break;
                }
                if(findDoc->fieldNames.array[j] == searchFields.array[i]){
                    fieldFound = 1;
                    if(operators.array[i] == 1){
                        if(findDoc->fields.array[j] != searchVals.array[i]){
                            matchQuery = 0;
                            break;
                        }   
                    } else if(operators.array[i] == 2){
                         if(findDoc->fields.array[j] >= searchVals.array[i]){
                            matchQuery = 0;
                            break;
                        }
                    }else if(operators.array[i] == 3){
                         if(findDoc->fields.array[j] <= searchVals.array[i]){
                            matchQuery = 0;
                            break;
                        }
                    }
                }
            }
            if(fieldFound == 0){
                fieldExists = 0;
            }

        }
        int printedID = 0;
        if(matchQuery == 1 && fieldExists == 1){
            int didPrint = 0;
            if(projections.array[0] == 'X'){
                printf("A: %d ", findDoc->id);
                printedID = 1;
            }
            for(int i = 0; i < projections.used; i++){
                if(projections.array[i] == 'A' && printedID == 0){
                    printf("A: %d ", findDoc->id);
                    printedID = 1;
                }
            }
            for(int i = 0; i < findDoc->fieldNames.used; i++){
                for(int j = 0; j < projections.used; j++){
                    if(projections.array[0] == 'X' || findDoc->fieldNames.array[i] == projections.array[j]){
                        
                        if(projections.array[j] == 'A' && printedID == 1)
                            break;
                        else
                        printf("%c: %d ", findDoc->fieldNames.array[i], findDoc->fields.array[i]);
                        didPrint = 1;
                        break;
                    }
                }
            }
            if(didPrint == 1)
                printf("\n");
        }
        findDoc = findDoc->next;
    }
    printf("\n");

}


void mergeInt(Array elm, Array elmID, int l, int m, int r){
    int i, j, k;
    int n1 = m - l + 1;
    int n2 =  r - m;
    int L[n1], R[n2], LID[n1], RID[n2];

    for (i = 0; i < n1; i++){
        L[i] = elm.array[l + i];
        LID[i] = elmID.array[l+i];
    }
    for (j = 0; j < n2; j++){
        R[j] = elm.array[m + 1+ j];
        RID[j] = elmID.array[m+1+j];
    }
    i = 0;
    j = 0;
    k = l;
    while (i < n1 && j < n2){
        if (L[i] <= R[j]){
            elm.array[k] = L[i];
            elmID.array[k] = LID[i];
            i++;
        }
        else{
            elm.array[k] = R[j];
            elmID.array[k] = RID[j];
            j++;
        }
        k++;
    }
    while (i < n1){
        elm.array[k] = L[i];
        elmID.array[k] = LID[i];
        i++;
        k++;
    }
    while (j < n2){
        elm.array[k] = R[j];
        elmID.array[k] = RID[j];
        j++;
        k++;
    }
}

void sortInt(Array elm, Array elmID, int l, int r){
  if (l < r){
    int m = l+(r-l)/2;
    sortInt(elm, elmID, l, m);
    sortInt(elm, elmID, m+1, r);
    mergeInt(elm, elmID, l, m, r);
  }
}

void sort(FILE *fp, int security){
    Array elements;
    Array elementIds;
    initArray(&elements,5);
    initArray(&elementIds, 5);
    int order; //1 for Ascending, 2 for Descending
    char sortLine[10];
    char sortField;
    fgets(sortLine,10,fp);
    if(strstr(sortLine, "-1") != 0)
        order = 2;
    else
        order = 1;
    sortField = sortLine[0];
    Document *sortDoc = head;
    while(sortDoc != NULL){
        for(int i = 0; i < sortDoc->fieldNames.used; i++){
            if(sortDoc->fieldNames.array[i] == sortField && sortDoc->security <= security){
                insertArray(&elements, sortDoc->fields.array[i]);
                insertArray(&elementIds, sortDoc->id);
            }
        }
        sortDoc = sortDoc->next;
    }

    sortInt(elements, elementIds, 0, elements.used - 1);


    if(order == 1){
        for(int i = 0; i < elements.used; i++){
            sortDoc = head;
            while(sortDoc != NULL){
                if(elementIds.array[i] == sortDoc->id){
                    printf("A: %d ", sortDoc->id);
                    for(int j = 0; j < sortDoc->fields.used; j++){
                        printf("%c: %d ", sortDoc->fieldNames.array[j],sortDoc->fields.array[j]);
                    }
                    printf("\n");
                }
                sortDoc = sortDoc->next;
            }
        }
    }else{
        for(int i = elements.used - 1; i >= 0; i--){
            sortDoc = head;
            while(sortDoc != NULL){
                if(elementIds.array[i] == sortDoc->id){
                    printf("A: %d ", sortDoc->id);
                    for(int j = 0; j < sortDoc->fields.used; j++){
                        printf("%c: %d ", sortDoc->fieldNames.array[j],sortDoc->fields.array[j]);
                    }
                    printf("\n");
                }
                sortDoc = sortDoc->next;
            }
        }
    }
    printf("\n");
}

void populate()
{
    FILE *fptr;
    char doc[300];
    int numID = 1;
    fptr = fopen("data.txt", "r");
    if (fptr == NULL)
    {
        printf("data.txt file not found");
        exit(0);
    }
    while(!feof(fptr)){
        fgets(doc, 300, fptr);
        Document *newDoc = (Document *)malloc(sizeof(Document));
        int alphabet;
        char dataValue[10] = "";
        char temp[3];
        newDoc->id = numID;
        newDoc->next = NULL;
        initArray(&newDoc->fields,5);
        initArray(&newDoc->fieldNames,5);
        for(int i = 0; i < strlen(doc); i++){
            
            if(isalpha(doc[i]) != 0){
                insertArray(&newDoc->fieldNames, doc[i]);
                alphabet = doc[i];
            }
            if(isdigit(doc[i]) != 0){
                sprintf(temp, "%c", doc[i]);
                strncat(dataValue, temp,1);
            }
            if((doc[i+1] == '\0'||doc[i] == 10||(isspace(doc[i]) != 0 ))&& strcmp(dataValue, "") != 0){
                //printf("Space\n");
                //firstDoc->fields[alphabet] = atoi(dataValue);
                insertArray(&newDoc->fields, atoi(dataValue));
                if(alphabet == 'Y'){
                    newDoc->security = atoi(dataValue);
                }
                strcpy(dataValue, "");
            }
        }
        if(numID == 1){
            head = newDoc;
            current = newDoc;
        }else{
            current->next = newDoc;
            current = newDoc;
        }
        
        numID ++;
    }
    fclose(fptr);
}

int main()
{
    populate();
    FILE *fp;
    char query[50];
    char securityLevel[10] = "";
    char temp[3];
    int totalQueries = 0;
    int oneError = 0;
    fp = fopen("final.txt", "r");
    if (fp == NULL){
        printf("file.txt file not found");
        exit(0);
    }
    while(!feof(fp)){
        fgets(query, 50, fp);
        if(strstr(query, "FIND") != NULL || strstr(query, "FIND ") != NULL){
            totalQueries ++;
            printf("//Query %d\n", totalQueries);
            oneError = 0;
            for(int i = 0; i < strlen(query); i++){
                if(isdigit(query[i]) != 0){
                    sprintf(temp, "%c", query[i]);
                    strncat(securityLevel, temp,1);
                }else if(isdigit(query[i]) == 0 && strcmp(securityLevel, "") != 0){
                    find(fp, atoi(securityLevel));
                    break;
                }else if(isdigit(query[i]) == 0 && i == strlen(query) - 1){
                    find(fp,INT_MAX);
                } 
            }
        }else if(strstr(query, "SORT")!= NULL || strstr(query, "SORT ") != NULL){
            oneError = 0;
            totalQueries++;
            printf("//Query %d\n", totalQueries);
            for(int i = 0; i < strlen(query); i++){
                if(isdigit(query[i]) != 0){
                    sprintf(temp, "%c", query[i]);
                    strncat(securityLevel, temp,1);
                }else if(isdigit(query[i]) == 0 && strcmp(securityLevel, "") != 0){
                    sort(fp, atoi(securityLevel));
                    break;
                }else if(isdigit(query[i]) == 0 && i == strlen(query) - 1){
                    sort(fp,INT_MAX);
                } 
            }


        }else{
            if(oneError == 0){
                totalQueries ++;
                printf("//Query %d\n", totalQueries);
                printf("ERROR - No such operation\n");
                oneError = 1;
            }
        }
        strcpy(securityLevel, "");

    }
    fclose(fp);
    return 0;
}