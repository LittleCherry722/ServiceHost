from pydot import *
import re
import hashlib
import argparse

parser = argparse.ArgumentParser(description="Generate Call-Map out of a tracefile")
parser.add_argument("--trace", default="log_travel_request_reduce.log")
parser.add_argument("--dot", default="generated.dot")
parser.add_argument("--hide-line", action="store_true")
parser.add_argument("--hide-uuid", action="store_true")
parser.add_argument("--include-persistance", action="store_true")

args = parser.parse_args()
FILE_IN = args.trace
FILE_OUT = args.dot
SHOW_LINE = not args.hide_line
SHOW_UUID = not args.hide_uuid
INCLUDE_PERSISTANCE = args.include_persistance

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



def build_graph(creation, messages, clusters, filename):
    g = Dot("MyName", ranksep="1.5")
    add_clusters(g, clusters, messages)
    # Turn this to true to include creation-edges. Do not forget to add real colors in get_color(..)
    if False:
        add_edges(g, creation, "create", "create")
    add_edges(g, messages, "message", "message")

    #g.layout(prog='fdp')
    g.write_dot(filename)



def read_graph(filename):
    braces = re.compile("(\([^\)\(]*\))")
    persistance = re.compile("persistance")
    regex = re.compile("^(TRACE: from )(.*)( to )([^ ]*)( )(.*)$")

    inF = open(filename, 'r')

    data = []

    i = 0

    for line in inF:
        try:
            line = line.replace("\r", "").replace("\n", "")

            if INCLUDE_PERSISTANCE or persistance.search(line) is None:
                continue

            # remove all braces with their contents
            while braces.search(line) is not None:
                line = braces.sub("", line)


            arr = regex.findall(line)[0]
            a = arr[1]
            b = arr[3]
            msg = arr[5]

            data.append((i,a,b,msg))
        except Exception as e:
            print("can not parse line: " + line)

        i = i + 1

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

    messages = read_graph(FILE_IN)
    messages = make_actor_nodes(messages)
    messages = flat_messages(messages)

    clusters = {
            "MyCluster": ["ActStateActor", "ReceiveStateActor", "SendStateActor", "ArchiveStateActor", "BlockingActor", "EndStateActor", "EndStateActor", "GoogleSendProxyActor", "InternalBehaviorActor", "InternalBehaviorActor", "change"],
            #"foo": ["bar", {"fooz": ["baaz"]}],
            }

    # TODO: extract the creation from the raw actor names
    creation = []


    build_graph(creation, messages, clusters, FILE_OUT)
    print("wrote output to: " + FILE_OUT)
