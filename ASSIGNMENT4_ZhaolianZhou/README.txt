The structure of this folder:

1. README.txt -- Description.
2. ANSWERS.pdf -- All answers of the written part.
3. Part1 Code folder -- Include two java applications, template file, template explaination and the middle files used in part 1.
	And also the java code used to split the train files used in part2 question 2.
4. Part2Q2Result folder -- All the evaluate result with different train size  used to answer written part question 2.
5. Part2Q3Result folder -- All the accuracy result with different c value size to answer written part question 3.

Usage of code part:

1. Create a new java project in eclipse.
2. Import the NLPTool given on the wattle into the project.
3. Put all the three java files in Part1Code under /src/nlp/opennlp package.
4. Put the esp.train, extrectedNE.txt, testSet.txt, testResult.txt, textwithtag.txt files under the project folder.
5. Run the correspoding class.

Note: 

1. NERApplication.java used to transfer the plain text file into the row file and put the POSTagger.
2. formatResult.java used to chunker the certain name entity, count the frequency and extracted the information into file in order with certain format.
3. splitTrainFile.java is used in part2 question 2 to get the 1/4, 2/4, 3/4 set of the esp.train set.

