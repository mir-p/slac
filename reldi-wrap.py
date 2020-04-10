# -*- coding: utf-8 -*-
from reldiTagger import reldiTagger as rt
import nltk
import codecs
import sys
f = codecs.open(sys.argv[1], 'r', 'utf-8')
txt = f.read()
tokens = nltk.word_tokenize(txt)
tags = rt.tag(tokens)
print('starting')
print('...')
i = 0
for tgd in tags:
	print(tokens[i] +'\t' + tgd[0] + '\t' + tgd[1])
	i+=1
print('finished')
