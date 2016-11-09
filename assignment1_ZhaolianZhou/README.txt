Q2- Q5

Files included in the tar file:
*DocAnalysisIRLab
*gov-test-collection
* u5782545_ZhaolianZhou_Assignment1.pdf
*trec_eval.8.1
*ReferencesFiles

How To Generate retrieve result file

Step 1:
     Import the project into Eclipse
Step 2:
     Run the FileIndexBuilder.java
Step 3:
     Run the SimpleSearchRanker.java
Step 4:
     Open /gov-test-collection/qrels, file "retrieved2.txt" will be the result file produced.
     Be notice that file "retrieved.txt" is the original result file before modification. Which can be used as a contrast.

How to run trec_eval to evaluate the result
Step 1:
     Open Termimal
Step 2:
     Enter the path where file "trec_eval.8.1" locate.
Step 3:
     Run command line $make
Step 4: 
     Run command line $ ./trec_eval -q ../gov-test-collection/qrels/gov.qrels ../gov-test-collection/qrels/retrieved2.txt  > ../gov-test-collection/qrels/test_result2
Step 5:
     Open /gov-test-collection/qrels/test_result2, can see the evaluation of each query and overall performance.
     Notice that file "test_result" is the original evaluation file before modification. 


 
