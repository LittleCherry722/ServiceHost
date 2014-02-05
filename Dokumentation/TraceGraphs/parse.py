from pydot import *
import re
import hashlib
import sys

FILE_IN = "log_travel_request_reduce.log"
FILE_OUT = "generated.dot"

if len(sys.argv) > 1:
    FILE_IN = sys.argv[1]
if len(sys.argv) > 2:
    FILE_OUT = sys.argv[2]
if len(sys.argv) > 3:
    print("unexpected arguments")

def get_color(label, palette):
    c = ["blue3", "darkgreen", "brown", "olive", "darkmagenta", "darkslateblue", "darkorange", "maroon"]
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
                    # TODO: should be `beginsWith` or sth like that instead of `in`
                    if node in actor.get_name():
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
        g.add_edge(Edge(a, b, color=color, fontcolor=color, label=str(i)+") "+msg))



def build_graph(creation, messages, clusters):
    g = Dot("MyName", ranksep="1.5")
    add_clusters(g, clusters, messages)
    # Turn this to true to include creation-edges. Do not forget to add real colors in get_color(..)
    if False:
        add_edges(g, creation, "create", "create")
    add_edges(g, messages, "message", "message")

    #g.layout(prog='fdp')
    g.write_dot(FILE_OUT)



def test_graph():
    creation = [
        ("A", "B", "c1"),
        ("A", "C", "c2"),
        ("B", "D", "c3")
        ]

    messages = [
        ("A", "B", "a1"),
        ("A", "C", "a2"),
        ("C", "D", "c1"),
        ]

    build_graph(creation, messages)



def read_graph():
    braces = re.compile("(\([^\)\(]*\))")
    regex = re.compile("^(TRACE: from )(.*)( to )([^ ]*)( )(.*)$")

    inF = open(FILE_IN, 'r')

    data = []

    i = 0

    for line in inF:
        line = line.replace("\r", "").replace("\n", "")

        # remove all braces with their contents
        while braces.search(line) is not None:
            line = braces.sub("", line)


        arr = regex.findall(line)[0]
        a = arr[1]
        b = arr[3]
        msg = arr[5]

        data.append((i,a,b,msg))
        i = i + 1

    return data

def actor_node(a):
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
        a_ = a_ + "____"  +guid.split("-")[0]


    s = a_.split("/")
    l = len(s)
    if "temp" in a_:
        a_ = "temp_" + s[l-1].split("]")[0]
    else:
        a_ = s[l-1]

    a_ = a_.strip()

    color = get_color(a_, "node")
    node = Node(a_, color=color, fontcolor=color)

    return node

def flat_message(msg):
    if not "." in msg:
        return msg.strip()

    msg_ = msg.split(";")[0]
    s = msg_.split(".")
    msg_ = s[len(s)-1]

    return msg_.strip()

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
    messages = read_graph()
    messages = make_actor_nodes(messages)
    messages = flat_messages(messages)

    clusters = {
            "MyCluster": ["ReceiveStateActor", "SendStateActor", "ArchiveStateActor", "BlockingActor", "EndStateActor", "EndStateActor", "GoogleSendProxyActor", "InternalBehaviorActor", "InternalBehaviorActor", "change"],
            #"foo": ["bar", {"fooz": ["baaz"]}],
            }

    creation = []


    build_graph(creation, messages, clusters)

#if __name__ == '__main__':
    #test_graph()
