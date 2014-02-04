from pydot import *
import re


#FILE_IN = "sample_input.log"
FILE_IN = "log_travel_request_reduce.log"
FILE_TEMPLATE = "template.dot"
FILE_OUT = "generated.dot"
COLOR_CREATION = "red"
COLOR_MESSAGES = "blue"

# TODO: we need more colors, eg for each message type

def add_edges(g, edges, key_suffix, color="black"):
    for (a, b, m) in edges:
        g.add_edge(Edge(a, b, key=a+"_"+b+"__"+key_suffix, color=color, fontcolor=color, label=m))



def build_graph(creation, messages):
    g = Dot("MyName", ranksep="1.5")
    add_edges(g, creation, "create", COLOR_CREATION)
    add_edges(g, messages, "message", COLOR_MESSAGES)

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
    creation = [
        ]

    messages = data

    build_graph(creation, messages)



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
