# japg - Just a Parser Generator
A zero-dependency EBNF parser generator.

## How to use
You can download a pre-built fat-jar from [GitHub releases](https://github.com/Ledmington/japg/releases) or you can build it yourself with:
```bash
./gradlew fatjar
```

Then, make it generate the parser from your EBNF grammar file:
```bash
java -jar japg-cli-0.1.0.jar -g <your_grammar_file> -o your/package/MyParser
```
This will create a java file called `MyParser.java` inside the existing `your/package` directory.

Finally, use it in your code like so:
```bash
package your.package;
public class MyClass {
	public static void main(String[] args) {
		final String text = Files.readString(args[0]);
		System.out.println(MyParser.parse(text));
	}
}
```

## How to contribute
```bash
./gradlew build
```
