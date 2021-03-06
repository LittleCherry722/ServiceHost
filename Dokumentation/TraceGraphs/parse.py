from pydot import *
import re
import hashlib
import argparse

parser = argparse.ArgumentParser(description="Generate Call-Map out of a tracefile")
parser.add_argument("--trace", default="../../Backend/log/trace.log", help="input file")
parser.add_argument("--dot", default="trace_{0}.dot", help="output file template ({0} gets replaced)")
parser.add_argument("--hide-line", action="store_true", help="Aggregate multiple messages between nodes (default: each message is shown with its line in the TRACE file)")
parser.add_argument("--hide-uuid", action="store_true", help="Aggregate multiple instances of an actor in one node (default: per instance one node)")
parser.add_argument("--include-persistence", action="store_true", help="Include Actors with `persistence` in their name (default: false)")
parser.add_argument("--include-temp", action="store_true", help="Include Actors with `temp` in their name (default: false)")
parser.add_argument("--aggregate", action="store_true", help="Aggregate all requests (default: false)")
parser.add_argument("--skip-lines", default="0", help="skip first SKIP_LINES of the TRACE file (default: 0)")

args = parser.parse_args()
FILE_IN = args.trace
FILE_OUT = args.dot
SHOW_LINE = not args.hide_line
SHOW_UUID = not args.hide_uuid
INCLUDE_PERSISTENCE = args.include_persistence
INCLUDE_TEMP = args.include_temp
AGGREGATE = args.aggregate
SKIP_LINES = int(args.skip_lines)

def get_color(label, palette):
#    c = ["blue3", "darkgreen", "brown", "olive", "darkmagenta", "darkslateblue", "darkorange", "maroon"]
    c = ["blue3", "darkgreen", "brown", "darkslateblue", "darkorange", "maroon"]
    colors = {
      "node": c,
      "create": ["white"],
      "message": c,
    }
    if palette not in colors:
        return "black"
    p = colors[palette]
    h = int(hashlib.sha1(label).hexdigest(), 16) % len(p)
    return p[h]

def add_clusters(g, clusters, messages, actors=None):
    if actors is None:
        actors = set()
        for (i, a, b, msg) in messages:
            actors.add(a)
            actors.add(b)
        add_to_g = set(actors)
    else:
        add_to_g = set()

    for k in clusters.keys():
        s = Subgraph('cluster_%s' % k,label=k)

        subs = []
        for node in clusters[k]:
            if type(node) is dict:
                subs.append(node)
            else:
                for actor in actors:
                    if actor.get_name().startswith(node):
                        s.add_node(actor)
                        add_to_g.discard(actor)

        for sub in subs:
            add_clusters(s, sub, messages, actors)

        g.add_subgraph(s)

    for node in add_to_g:
        g.add_node(node)


def add_edges(g, edges, key_suffix, colorpalette):
    for (i, a, b, msg) in edges:
        color = get_color(msg, colorpalette)
        if SHOW_LINE:
            g.add_edge(Edge(a, b, color=color, fontcolor=color, label=str(i)+") "+msg))
        else:
            a_ = a.get_name().replace("-","").replace("$","")
            b_ = b.get_name().replace("-","").replace("$","")
            g.add_edge(Edge(a, b, color=color, fontcolor=color, label=msg, key=a_+"_"+b_+"_"+msg))



def build_graph(label, creation, messages, clusters, filename):
    g = Dot(label, label=label, ranksep="1.5")
    add_clusters(g, clusters, messages)
    # Turn this to true to include creation-edges. Do not forget to add real colors in get_color(..)
    if False:
        add_edges(g, creation, "create", "create")
    add_edges(g, messages, "message", "message")

    #g.layout(prog='fdp')
    g.write_dot(filename)



def read_graph(filename):
    braces = re.compile("(\([^\)\(]*\))")
    persistence = re.compile("persistence")
    temp = re.compile("temp")
    regex_request = re.compile("^(TRACE: request )(.*)$")
    regex_from = re.compile("^(TRACE: from )(.*)( to )([^ ]*)( )(.*)$")

    inF = open(filename, 'r')

    data = {0: (0, "", [])}
    current_request = 0

    for _ in xrange(SKIP_LINES):
        next(inF)

    i = SKIP_LINES

    for line in inF:
        i = i + 1

        try:
            line = line.replace("\r", "").replace("\n", "")

            arr = regex_request.findall(line)
            if len(arr) > 0 and not AGGREGATE:
                current_request = i
                data[current_request] = (i, arr[0][1], [])

            arr = regex_from.findall(line)
            if len(arr) > 0:
                if not (persistence.search(line) is None) and not INCLUDE_PERSISTENCE:
                    continue

                if not (temp.search(line) is None) and not INCLUDE_TEMP:
                    continue

                # remove all braces with their contents
                while braces.search(line) is not None:
                    line = braces.sub("", line)

                a = arr[0][1]
                b = arr[0][3]
                msg = arr[0][5]
                data[current_request][2].append((i,a,b,msg))
        except Exception as e:
            print("can not parse line: " + line)

    return data

def get_actor_name(a):
    # TODO: regex maybe better
    a_ = a.split("(")[0]


    s = a_.split("#")
    l = len(s)
    if l == 1:
        a_ = s[0]
    else:
        a_ = s[l-2]


    s = a_.split("____")
    l = len(s)
    if l == 1:
        a_ = s[0]
    else:
        guid = s[l-1]
        a_ = s[l-2]
        # take first part of guid
        if SHOW_UUID:
            a_ = a_ + "____"  +guid.split("-")[0]


    s = a_.split("/")
    l = len(s)
    if "temp" in a_:
        a_ = "temp_" + s[l-1].split("]")[0]
    else:
        a_ = s[l-1]

    a_ = a_.strip()

    return a_

def actor_node(a):
    a_ = get_actor_name(a)
    color = get_color(a_, "node")
    node = Node(a_, color=color, fontcolor=color)

    return node

def flat_message(msg):
    msg_ = get_actor_name(msg)

    if not "." in msg_:
        return msg_

    msg_ = msg.split(";")[0]
    s = msg_.split(".")
    msg_ = s[len(s)-1]


    return msg_

def make_actor_nodes(messages):
    messages_ = []
    for (i,a,b,msg) in messages:
        a_ = actor_node(a)
        b_ = actor_node(b)
        messages_.append((i,a_,b_,msg))

    return messages_

def flat_messages(data):
    data_flat_messages = []
    for (i,a,b,msg) in data:
        msg_ = flat_message(msg)
        data_flat_messages.append((i,a,b,msg_))

    return data_flat_messages

if __name__ == '__main__':
    print("read tracefile from: " + FILE_IN)

    for line, request, messages in read_graph(FILE_IN).itervalues():
        if len(messages) == 0:
            continue
        messages = make_actor_nodes(messages)
        messages = flat_messages(messages)
        
        clusters = {
                "StateActors": ["ActStateActor", "ReceiveStateActor", "SendStateActor", "ArchiveStateActor", "BlockingActor", "EndStateActor", "EndStateActor", "GoogleSendProxyActor", "InternalBehaviorActor", "InternalBehaviorActor", "DecisionStateActor", "change"],
                #"foo": ["bar", {"fooz": ["baaz"]}],
                }

        # TODO: extract the creation from the raw actor names
        creation = []

        file_out = FILE_OUT.format(line)
        build_graph("{0} ({1})".format(request, line), creation, messages, clusters, file_out)
        print("wrote output to: " + file_out)
