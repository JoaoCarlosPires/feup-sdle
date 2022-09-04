import argparse, os, signal, sys, time, re
from threading import Thread, Lock

parser = argparse.ArgumentParser()

parser.add_argument("-nSubs", dest="nSubs", default=0, type=int, help="the number of subscribers")
parser.add_argument("-nPubs", dest="nPubs", default=0, type=int, help="the number of publishers")
parser.add_argument("-savingPeriod", dest="savingPeriod", default=0, type=int, help="the saving period, in seconds - if == 0, then state is saved whenever any change happens")

BROKER = "BROKER"
PUBLISHER = "PUB"
SUBSCRIBER = "SUB"

args = parser.parse_args()

entityColors = [ '\033[1;33m', '\033[1;34m', '\033[1;36m', '\033[1;32m', '\033[1;35m' ]

class PrintPeerOutput(Thread):
    def __init__(self, proc, lock, stdout=True):
        Thread.__init__(self)
        self.running = True
        self.proc = proc
        self.lock = lock
        self.stdout = stdout

        self.name = str(self.proc["indexationId"])
        self.spacer = "-"
        for _n in self.name: self.spacer += "-"

        colorId = self.proc["id"] % len(entityColors)
        if self.proc["type"] == SUBSCRIBER: colorId = len(entityColors) - colorId - 1
        self.color = entityColors[colorId]

    def print_line(self, line, first = False):
        if (not self.stdout): print('\033[41m\033[1;37m', end="") # ansi color code for red background
        elif self.proc["type"] == BROKER: print('\033[42m\033[1;37m', end="")
        else: print(self.color, end="")

        print((self.name + ":" if first else self.spacer) + "\033[0m ", end="") # prints the left margin
        print(line) # prints the line itself

    def print_lines(self, lines):
        startIdx = 0
        while startIdx < len(lines) and lines[startIdx].strip() == "":
            startIdx += 1

        if len(lines) == startIdx:
            return

        for line in lines[startIdx:]:
            if line.strip() == "": continue
            self.print_line(line, line == lines[startIdx])

        print()

    def run(self):
        time.sleep(.5)
        while self.running:
            text = os.read(self.proc["stdoutPipeRFD" if self.stdout else "stderrPipeRFD"], 10240).decode("UTF-8")
            if (len(text) != 0):
                self.lock.acquire() # so that they don"t interrupt each other
                self.print_lines(text.split("\n"))
                self.lock.release()
            time.sleep(.2)

    def stop(self):
        self.running = False

def run_gradle(className, args=None):
    runArgs = [
        "gradle",
        "run",
        "--quiet",
        f'-Pfile=pt/up/fe/sdle/reliableps/{className}'
    ]

    if args != None:
        runArgs.append(f'--args="{" ".join(args)}"')

    os.execvp("gradle", runArgs)

def run_subscriber(id):
    run_gradle("Subscriber", [ str(id) ])

def run_publisher(id):
    run_gradle("Publisher", [ str(id) ])

def run_broker():
    run_gradle("Broker", [ str(args.savingPeriod) ])

processes = {}
lock = Lock()

def get_process_indexation_id(type, entityId=""):
    return type + str(entityId)

def start_entity(indexationId, runFunction, type, entityId=0):
    stdoutRead, stdoutWrite = os.pipe()
    stderrRead, stderrWrite = os.pipe()
    stdinRead, stdinWrite = os.pipe()
    newpid = os.fork()

    if newpid == 0:  # peer
        os.close(stdoutRead)
        os.close(stderrRead)
        os.close(stdinWrite)
        os.dup2(stdoutWrite, sys.stdout.fileno())  # stdout will be the pipe
        os.dup2(stderrWrite, sys.stderr.fileno())  # stderr will be the pipe
        os.dup2(stdinRead, sys.stdin.fileno())
        runFunction()
    else:  # parent
        os.close(stdoutWrite)
        os.close(stderrWrite)
        os.close(stdinRead)
        processes[indexationId] = {
            "type": type,
            "id": entityId,
            "indexationId": indexationId,
            "pid": newpid,
            "stdoutPipeRFD": stdoutRead,
            "stderrPipeRFD": stderrRead
        }

    thread1 = PrintPeerOutput(processes[indexationId], lock)
    thread2 = PrintPeerOutput(processes[indexationId], lock, False)
    processes[indexationId]["threads"] = [ thread1, thread2 ]
    for thread in processes[indexationId]["threads"]:
        thread.setDaemon(True)
        thread.start()


def stop_entity(entityId, type, deleteFromProcesses=False):
    indexationId = get_process_indexation_id(entityId, type)

    stop_entity_process(processes[indexationId], True)
    if (deleteFromProcesses):
        del processes[indexationId]

def stop_entity_process(proc, wait=False):
    for thread in proc["threads"]:
        thread.stop()
    os.close(proc["stdoutPipeRFD"])
    os.close(proc["stderrPipeRFD"])
    os.kill(proc["pid"], signal.SIGTERM)
    if wait: os.wait()

def start_entities():
    start_entity(BROKER, run_broker, BROKER)
    for i in range(0, args.nSubs):
        start_entity(get_process_indexation_id(SUBSCRIBER, i), lambda: run_subscriber(i), SUBSCRIBER, i)
    for i in range(0, args.nPubs):
        start_entity(get_process_indexation_id(PUBLISHER, i), lambda: run_publisher(i), PUBLISHER, i)
    print("\nCreated processes: \n" + str(processes), end="\n\n")


def close_processes():
    for proc in processes.values():
        stop_entity_process(proc)
    for proc in processes.values():
        os.wait()

def printTips():
    print("Insert 'exit' to close the service. Press ENTER to recompile.\n")

running = True

printTips()

regularExpressions = {
    PUBLISHER: re.compile("^" + PUBLISHER + "(.*)$"),
    SUBSCRIBER: re.compile("^" + SUBSCRIBER + "(.*)$")
}

while running:
    start_entities()

    while True:
        text = sys.stdin.readline().strip()

        if text == "exit":
            running = False
            break
        elif text == "state":
            newpid = os.fork()
            if newpid == 0:
                os.execvp("gradle", [ "gradle", "run", "--quiet", "--console=plain", f"-Pfile=pt/up/fe/sdle/reliableps/testapp/BrokerTestApp" ])
            continue
        elif text != "":
            match = regularExpressions[PUBLISHER].search(text)
            isPublisher = True
            if match == None:
                match = regularExpressions[SUBSCRIBER].search(text)
                isPublisher = False

            if match == None:
                print("You can only send commands to PUB<ID> or SUB<ID>")
                continue

            appName = "SubscriberTestApp" if not isPublisher else "PublisherTestApp"

            print()
            newpid = os.fork()
            if newpid == 0:
                os.execvp("gradle", [ "gradle", "run", "--quiet", "--console=plain",
                    f"-Pfile=pt/up/fe/sdle/reliableps/testapp/{appName}", f'--args="{match.group(1)}"' ])
            continue
        break # restart / compile

    close_processes()