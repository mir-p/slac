# SLAC
SLAC - Slavic Collocation Analyser: Simple Corpus Manager (TEI-XML compatible):
- handles TXT and basic <a href="https://tei-c.org/">TEI XML</a> (<strong>w</strong> tag + <em>lemma, pos</em> attributes)
- handles lemmatised files
- generates frequency lists
- calculates connotations using log-likelihood (Cantos Gomez 2013: 185-195), mutual information, t-score (Stubbs 1995), and log-ratio (Hardie 2014)
- provides concordance lines with 2-level left/right sorting
- includes file preview option

<a href="https://github.com/mir-p/slac/blob/master/SLAC.jar">Download JAR binary (Java, ~52 KB)</a>

# SCRAPPER
Scrapper is a small application for scrapping articles from newspaper websites and generating samples for analysis. The samples can be randomised and/or temporally limited. 

Scripts for Scrapper are written in <a href="https://www.python.org/downloads/">Python 3</a>. System `PATH` should contain python3. Most of the scripts depend on following libraries: `requests, lxml, newspaper3k, sqlite3`. In order to install them, a package manager PIP3 should be present on the machine. Then following commands should be run in shell:
`pip3 install requests lxml newspaper3k sqlite3`

On Linux Scrapper should be run through shell (`java -jar SCRAPPER.jar`) in order to set the current folder as user directory and access the scripts.

Main Scrapper tab calls on one of the scripts from the application's 'resources' directory (accompanied by a configuration XML file, which provides a source URL, newspaper title, and the default number of the starting page on the respective website).  The script is called in python3 with parameters referring to: 1) target SQLITE3 database, 2) source URL, 3) starting page in the source website.

The underlying idea is to open a website of a respective newspaper (column) at its given starting page. The script then mines for links to particular articles on the given page. The articles are then scrapped and mined for their titles, authors, publication dates, and content. These data are inserted into a target database. When finished with all articles on the page, the next one is loaded and the same procedure is repeated.

If target database does not exists, before starting scripts it should be created by clicking "Create" button. Output of the Python script is flushed into the Scrapper window and the process can be stopped at every time ("Stop" button).

Downsampler tab enables to open databases generated by Scrapper. Subcorpora contained in determined temporal limits can be generated there, they can be also limited to a predefined number of random samples. It also offers a possibilty to check the number of articles in a given database slice. Note that the target database should exist (or be created by clicking "Create" button) before generating a subsample.

<a href="https://github.com/mir-p/slac/blob/master/SCRAPPER.zip">Download zipped JAR binary (ZIP, ~6,1 MB)</a>

# LEMING
LEMING is a simple GUI for lemmatisers. It is primarily dedicated for lemmatising databases created by SCRAPPER. The application can also handle collections of text files. LEMING depends on external lemmatisers - it was tested with <a href="https://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/">TreeTagger</a>, which provides support for numerous languages, including English, Polish, Russian, Czech, Bulgarian, Slovak, and Slovenian. A Python2 script processing I/O and cooperating with <a href="https://github.com/clarinsi/reldi-tagger">ReLDI Tagger</a> for Croatian, Serbian, and Slovenian is also available. 

Follow the instructions concerning installation of TreeTagger (pay attention to its licensing rules). If TreeTaggger is present in the system's PATH variable, the field Path/Language should contain simply `tree-tagger-{language}`, e.g. `tree-tagger-polish`. If we use ReLDI-Tagger, given it's in the same directory as LEMING, we can call it through *reldi-wrap.py* script, i.e. Path/Language field should contain the text `python2 reldi-wrap.py`.

For now, LEMING does not show lemmatisation progress. It's frozen until it finishes processing all content from the database or each text file loaded. 

<a href="https://github.com/mir-p/slac/blob/master/LEMING.java">Download JAR binary (Java, ~6,1 MB)</a>

## Literature
- P. Cantos Gomez (2013) Statistical Methods in Language and Linguistic Research. Oakville: Equinos. 
- M. Stubbs (1995)  Collocations and  Semantic Profiles, Functions of Language 2 (1): 23-55.
- A. Hardie (2014) <a href="http://cass.lancs.ac.uk/log-ratio-an-informal-introduction/">Log Ratio - an informal introduction</a>.
