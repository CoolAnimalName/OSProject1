sourcefiles = \
CPU.java \
Memory.java

classfiles = $(sourcefiles:.java=.class)

all: $(classfiles)
%.class: %.java
		javac -g . -classpath . $<

clean:
	rm -f *.class
