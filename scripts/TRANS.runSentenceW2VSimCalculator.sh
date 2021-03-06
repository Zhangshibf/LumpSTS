source=$1
target=$2
path='/home/cristinae/pln/LumpSTS/task1/'

embeddings='/media/cristinae/DATA1/pln/semeval/w2v/corpusAll.all.b.300.w2v'
embeddingsLAN='/media/cristinae/DATA1/pln/semeval/w2v/corpusAll.ar.b.300.w2v'
simPath=${source%cris/*}
simPath=${simPath}'sim/ar-ar.0.5-'
#simPath=${simPath}'sim/'$3'.0.5-'
#tmp=${a#*_}   # remove prefix ending in "_"
#b=${tmp%_*}   # remove suffix starting with "_"

java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddings  -s $path$source -t $path$target -m cosine -o $path${simPath}BNallcosine
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddings  -s $path$source -t $path$target -m jaccard -o $path${simPath}BNalljaccard
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddings  -s $path$source -t $path$target -m KL -o $path${simPath}BNallkl
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddings  -s $path$source -t $path$target -m JS -o $path${simPath}BNalljs


java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddingsLAN  -s $path$source -t $path$target -m cosine -o $path${simPath}BNarcosine
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddingsLAN  -s $path$source -t $path$target -m jaccard -o $path${simPath}BNarjaccard
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddingsLAN  -s $path$source -t $path$target -m KL -o $path${simPath}BNarkl
java -cp target/LumpSTS-0.0.1-SNAPSHOT-jar-with-dependencies.jar cat.lump.sts2017.similarity.SentenceW2VSimCalculator -e $embeddingsLAN  -s $path$source -t $path$target -m JS -o $path${simPath}BNarjs

