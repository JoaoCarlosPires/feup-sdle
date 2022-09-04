COMPILING INSTRUCTIONS

To compile, just enter the command 'gradle build' under the first \src directory (not the \src\src).

RUNNING INSTRUCTIONS

- Script to run multiple publisher and clients in one command line is located in src\run.py.

1. Execution

usage: python3 run.py [-h] [-nSubs NSUBS] [-nPubs NPUBS] [-savingPeriod SAVINGPERIOD]

optional arguments:
  -h, --help            show this help message and exit
  -nSubs NSUBS          the number of subscribers
  -nPubs NPUBS          the number of publishers
  -savingPeriod SAVINGPERIOD
                        the saving period, in seconds - if == 0, then state is saved whenever any change happens

2. While inside the script, insert...

... exit: to exit the script
... state: to print the state of the broker (topics, subscribers and content)
... ENTER (just press enter key): to restart the execution of all entities
... PUB<ID> or SUB<ID> plus the test app arguments to execute a test app command to the referenced entity.

Example:

'SUB1 SUBSCRIBE topic' will call the subscriber test app with the arguments "1 SUBSCRIBE topic", which will result in the subscriber with ID=1 being subscribed to that topic

- Scripts to run the program manually are located in 'src\scripts\'.

Run the broker: `./broker.sh`
Run the subscriber: `./subscriber.sh <subscriberId>`
Run the publisher: `./publisher.sh <publisherId>`

- Scripts to run the developed test applications with RMI are located in 'src\scripts\'.

Run broker testapp: `./testbroker.sh`
Run subscriber testapp: `./testsub.sh <subscriberId> <action> <topic>`
Run publisher testapp: `./testpub.sh <publisherId> <topic> <content>`
