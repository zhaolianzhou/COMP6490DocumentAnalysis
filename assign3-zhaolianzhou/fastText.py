#!/usr/bin/python

import collections
import math
import os
import random

import numpy as np
import tensorflow as tf
from random import shuffle
from nltk import word_tokenize #to implete tokenize() method
import sys, getopt
import os

from collections import namedtuple

Dataset = namedtuple('Dataset','sentences labels')

num_classes = 3
learning_rate = 0.01
num_epochs = 10
embedding_dim = 10
label_to_id = {'World':0, 'Entertainment':1, 'Sports':2}
unknown_word_id = 0
def create_label_vec(label):
   # Generate a label vector for a given classification label.
   label_vec = [0]*num_classes
   real_label = label.strip()
   label_vec[label_to_id[real_label]] = 1
   return label_vec


def tokenize(sens):
    # Tokenize a given sentence into a sequence of tokens.
    # Return the list of tokens.
    return word_tokenize(sens)

def map_token_seq_to_word_id_seq(token_seq, word_to_id):
    return [map_word_to_id(word_to_id,word) for word in token_seq]


def map_word_to_id(word_to_id, word):
    # map each word to its id.
    if word in word_to_id:
        return word_to_id[word]
    else:
        return unknown_word_id


def build_vocab(sens_file_name):
    data = []
    with open(sens_file_name) as f:
        for line in f.readlines():
            tokens = tokenize(line)
            data.extend(tokens)
    count = [['$UNK$', 0]]
    sorted_counts = collections.Counter(data).most_common()
    count.extend(sorted_counts)
    word_to_id = dict()
    for word, _ in count:
        word_to_id[word] = len(word_to_id)
    print('size of vocabulary is %s. ' % len(word_to_id))
    return word_to_id


def read_labeled_dataset(sens_file_name, label_file_name, word_to_id):
    sens_file = open(sens_file_name)
    label_file = open(label_file_name)
    data = []
    for label in label_file:
        sens = sens_file.readline()
        word_id_seq = map_token_seq_to_word_id_seq(tokenize(sens), word_to_id)
        data.append((word_id_seq, create_label_vec(label)))
    print("read %d sentences from %s ." % (len(data), sens_file_name))
    sens_file.close()
    label_file.close()
    return data

def read_dataset(sens_file_name, word_to_id):
    sens_file = open(sens_file_name)
    data = []
    for sens in sens_file:
        word_id_seq = map_token_seq_to_word_id_seq(tokenize(sens), word_to_id)
        data.append(word_id_seq)
    print("read %d sentences from %s ." % (len(data), sens_file_name))
    sens_file.close()
    return data


def eval(word_to_id, train_dataset, dev_dataset, test_dataset):
    num_words = len(word_to_id)
    # Initialize the placeholders and Variables. E.g.
    correct_label = tf.placeholder(tf.float32, shape=[num_classes])
    # Hint: use [None] when you are not certain about the value of shape
    #zzl placeholder
    input_ngram = tf.placeholder(tf.int32, shape=[None])
    #zzl Create a variable for label vector
    embeddings = tf.Variable(tf.random_uniform([num_classes, embedding_dim],-1.0,1.0))
    #zzl Create a variable for word
    embeddings_input = tf.Variable(tf.random_uniform([num_words, embedding_dim],-1.0,1.0))
    test_results = []

    with tf.Session() as sess:
        # Write code for constructing computation graph here.
        # Hint:
        #    1. Find the math operations at https://www.tensorflow.org/versions/r0.10/api_docs/python/math_ops.html
        #    2. Try to reuse/modify the code from tensorflow tutorial.
        #    3. Use tf.reshape if the shape information of a tensor gets lost during the contruction of computation graph.

        #zzl Looks up ids in a list of embedding tensor
        #This function is used to perform parallel lookups on the list of tensors in params.
        #It is a generalization of tf.gather(), where params is interpreted as a partition of a larger embedding tensor.
        embed = tf.nn.embedding_lookup(embeddings_input,input_ngram)
        #Math
        #tf.reduce_sum(input_tensor, reduction_indices=None, keep_dims=False, name=None)
        # Computes the sum of elements across dimensions of a tensor.
        #If reduction_indices has no entries, all dimensions are reduced, and a tensor with a single element is returned.
        tmp_m = tf.reduce_sum(embed,0)
        #tf.reshape(tensor, shape, name=None)
        #Reshapes a tensor.
        #Given tensor, this operation returns a tensor that has the same values as tensor with shape shape.
        sum_rep = tf.reshape(tmp_m,[1, embedding_dim])

        #zzl tf.nn.softmax(logits, name=None)
        # Computes softmax activations.
        # tf.matmul(a, b, transpose_a=False, transpose_b=False, a_is_sparse=False, b_is_sparse=False, name=None)
        # Multiplies matrix a by matrix b, producing a * b.
        # Formulate word embedding learning as a word prediction
        #The inputs must be two-dimensional matrices, with matching inner dimensions, possibly after transposition.
        #Both matrices must be of the same type.
        #Either matrix can be transposed on the fly by setting the corresponding flag to True.
        y = tf.nn.softmax(tf.matmul(sum_rep,embeddings,transpose_b = True))
        # tf.reduce_mean(input_tensor, reduction_indices=None, keep_dims=False, name=None)
        # Computes the mean of elements across dimensions of a tensor.
        cross_entropy = tf.reduce_mean(-tf.reduce_sum(correct_label * tf.log(y), reduction_indices=[1]))

        #evaluation code, assume y is the estimated probability vector of each class
        correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(correct_label, 0))
        #tf.cast(x, dtype, name=None)
        #Casts a tensor to a new type.
        #The operation casts x (in case of Tensor) or x.values (in case of SparseTensor) to dtype.
        accuracy = tf.cast(correct_prediction, tf.float32)
        prediction = tf.cast(tf.argmax(y, 1), tf.int32)

        sess.run(tf.initialize_all_variables())
        # In this assignment it is sufficient to use GradientDescentOptimizer, you are not required to implement a regularizer.
        #zzl Construct the SGD optimizer using a learning rate of 1.0
        optimizer = tf.train.GradientDescentOptimizer(learning_rate).minimize(cross_entropy)

        for epoch in range(num_epochs):
            shuffle(train_dataset)
            shuffle(dev_dataset)
            # Writing the code for training. It is not required to use a batch with size larger than one.
            for (sens, label) in train_dataset:
                optimizer.run(feed_dict = {input_ngram: sens, correct_label: label})
            # The following line computes the accuracy on the development dataset in each epoch.
            print('Epoch %d : %s .' % (epoch,compute_accuracy(accuracy,input_ngram, correct_label, dev_dataset)))

        # uncomment the following line in the grading lab for evaluation
        print('Accuracy on the test set : %s.' % compute_accuracy(accuracy,input_ngram, correct_label, test_dataset))
        # input_sens is the placeholder of an input sentence.
        test_results = predict(prediction, input_ngram, test_dataset)
    return test_results


def compute_accuracy(accuracy,input_sens, correct_label, eval_dataset):
    num_correct = 0
    for (sens, label) in eval_dataset:
        num_correct += accuracy.eval(feed_dict={input_sens: sens, correct_label: label})
    print('#correct sentences is %s ' % num_correct)
    return num_correct / len(eval_dataset)


def predict(prediction, input_sens, test_dataset):
    test_results = []
    for (sens, label) in test_dataset:
        test_results.append(prediction.eval(feed_dict={input_sens: sens}))
    return test_results


def write_result_file(test_results, result_file):
    with open(result_file, mode='w') as f:
         for r in test_results:
             if r ==0:
                 resultString = "World"
             elif r == 1:
                 resultString = "Entertainment"
             else:
                 resultString = "Sports"
             f.write("%s\n" %resultString)


def main(argv):
    trainSensFile = 'sentences_train.txt'
    trainLabelFile = 'labels_train.txt'
    devSensFile = 'sentences_dev.txt'
    devLabelFile = 'labels_dev.txt'
    testSensFile = 'sentences_test.txt'
    testLabelFile = 'labels_test.txt'
    testResultFile = 'test_results.txt'
    try:
        opts, args = getopt.getopt(argv,"hd:",["dataFolder="])
    except getopt.GetoptError:
        print('fastText.py -d <dataFolder>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('fastText.py -d <dataFolder>')
            sys.exit()
        elif opt in ("-d", "--dataFolder"):
            trainSensFile = os.path.join(arg, 'sentences_train.txt')
            devSensFile = os.path.join(arg, 'sentences_dev.txt')
            testSensFile = os.path.join(arg, 'sentences_test.txt')
            trainLabelFile = os.path.join(arg, 'labels_train.txt')
            devLabelFile = os.path.join(arg, 'labels_dev.txt')
            ## uncomment the following line in the grading lab
            testLabelFile = os.path.join(arg, 'labels_test.txt')
            testResultFile = os.path.join(arg, 'test_results.txt')
        else:
            print("unknown option %s ." % opt)
    ## Please write the main procedure here by calling appropriate methods.
    train_word_to_id = build_vocab(trainSensFile)
    train_set = read_labeled_dataset(trainSensFile, trainLabelFile, train_word_to_id)
    dev_word_to_id = build_vocab(devSensFile)
    dev_set = read_labeled_dataset(devSensFile, devLabelFile,train_word_to_id)

    test_word_to_id = build_vocab(testSensFile)
    #test_set = read_dataset(testSensFile,test_word_to_id)
    test_set = read_labeled_dataset(testSensFile, testLabelFile, train_word_to_id)
    test_results = eval(train_word_to_id, train_set, dev_set,test_set)
    write_result_file(test_results, testResultFile)

if __name__ == "__main__":
   main(sys.argv[1:])
