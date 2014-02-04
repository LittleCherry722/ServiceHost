from pydot import *
import re
import hashlib


#FILE_IN = "sample_input.log"
FILE_IN = "log_travel_request_reduce.log"
FILE_OUT = "generated.dot"

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

def add_clusters(g, clusters):
    for k in clusters.keys():
        s = Subgraph('cluster_%s' % k,label=k)

        subs = []
        for node in clusters[k]:
            if type(node) is dict:
                subs.append(node)
            else:
                color = get_color(node, "node")
                s.add_node(Node(node, color=color, fontcolor=color))

        for sub in subs:
            add_clusters(s, sub)

        g.add_subgraph(s)


def add_edges(g, edges, key_suffix, colorpalette):
    for (a, b, m) in edges:
        color = get_color(m, colorpalette)
        g.add_edge(Edge(a, b, key=a+"_"+b+"__"+key_suffix, color=color, fontcolor=color, label=m))



def build_graph(creation, messages, clusters):
    g = Dot("MyName", ranksep="1.5")
    add_clusters(g, clusters)
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



def render_graph(data):
    clusters = {
            "MyCluster": ["ReceiveStateActor____2428b3b7", "SendStateActor____83001d89", "ArchiveStateActor____14cf367b", "BlockingActor____064d3f35", "EndStateActor____aa9e1dac", "EndStateActor____16fe0625", "GoogleSendProxyActor____992361a1", "InternalBehaviorActor____4560303f", "InternalBehaviorActor____9b633e45", "change"],
            #"foo": ["bar", {"fooz": ["baaz"]}],
            }

    creation = [
        ]

    messages = data

    build_graph(creation, messages, clusters)



def read_graph():
    regex = re.compile("^(TRACE: from )(.*)( to )([^ ]*)( )(.*)$")

    inF = open(FILE_IN, 'r')

    data = []

    for line in inF:
        line = line.replace("\r", "").replace("\n", "")

        arr = regex.findall(line)[0]
        a = arr[1]
        b = arr[3]
        msg = arr[5]

        data.append((a,b,msg))

    return data

def flat_actor(a):
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

    return a_

def flat_message(msg):
    # TODO: regex maybe better
    msg_ = msg.split("(")[0]
    return msg_

def flat_actors(data):
    data_flat_actors = []
    for (a,b,msg) in data:
        a_ = flat_actor(a)
        b_ = flat_actor(b)
        data_flat_actors.append((a_,b_,msg))

    return data_flat_actors

def flat_messages(data):
    data_flat_messages = []
    for (a,b,msg) in data:
        msg_ = flat_message(msg)
        data_flat_messages.append((a,b,msg_))

    return data_flat_messages

if __name__ == '__main__':
    data = read_graph()
    actors_flat = flat_actors(data)
    messages_flat = flat_messages(actors_flat)

    render_graph(messages_flat)

#if __name__ == '__main__':
    #test_graph()
