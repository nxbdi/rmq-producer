Publish messages to an RMQ exchange. Input can be stdin or another RMQ queue.

## Build

This project is built with leiningen (http://leiningen.org/). Once you
have the source code, run "lein uberjar" from the command line to
build the jar file, which will be in the "target" directory.

## Usage

This will write {"x": "y"} to the exchange andy-test, and then
exit:

```
echo 'x,y' | java -jar rmq-producer-standalone.jar \
  --host rmq-host \
  --exchange rmq-test \
  --username rmquser \
  --password s3cur3 \
  --message-template '{"%s": "%s"}' \
  --field-separator ','
```

This will also write {"x": "y"} to the exchange andy-test, and then
exit:

```
echo '{"x": "y"}' | java -jar rmq-producer-standalone.jar \
  --host rmq-host \
  --exchange rmq-test \
  --username rmquser \
  --password s3cur3
```

This will read from the andy-test-src queue and write to the andy-test
exchange.

```
java -jar rmq-producer-standalone.jar \
  --host rmq-host \
  --source-queue rmq-queue \
  --exchange rmq-test \
  --username rmquser \
  --password s3cur3
```

To see a list of the command line options, run the command with no
arguments.

By default rmq-producer will send messages to the destination exchange
based on what it reads from standard input. You can over-ride this by
using the --source-queue argument to specify a queue from which to
read messages.

The user needs to specify the host and destination exchange. When
using stdin as the input, each line of input is read, split on the
field separator if one is specified, turned into a message based on
the message template, and published to the destination exchange.

### Field Separator

The program reads from stdin and, if a field separator is specified,
splits the input into parts for later processing. For example, if the
input line is:

```
  9,23,2013
```

and the field separator is ",", then the input will be split into a
list of strings like this:

```
  ["9", "23", "2013"]
```

The message template will then be applied to this list.

### Message Template

The message template is a string that is used to generate the final
message using Java's string format syntax. For example, if the input
line is:

```
  Andrew,Turley
```

and the message template is:

```
  {"firstName":"%s", "lastName":"%s"}
```

and field separator is ",", it will produce a message like this:

```
  {"firstName":"Andrew", "lastName":"Turley"}
```

If the message template is not specified, then the input is sent
directly to the destination exchange.

NOTE: Unless a source queue is specified, you must provide some sort
of input from standard input, otherwise the program will hang while it
waits for input. The easiest way to do this is to pipe a message in
like this:

```
  echo '{"key": "value"}' | java -jar rmq-producer-standalone.jar ...
```

### Source Queue

If a source queue is specified then the program reads messages from
the source queue and writes them directly to the destination exchange.

NOTE: When using a source queue, the routing key from the original
message will not be preserved. If a routing key is provided with the
`--routing-key` argument then that will be used, otherwise the new
message will not have a routing key.
