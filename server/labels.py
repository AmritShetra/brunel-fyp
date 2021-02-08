labels = [
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7"
]

# TODO: Fill this out - the chatbot's response to the photo
descriptions = [
    "",
    "",
    "",
    "",
    "",
    "",
    ""
]


def get_desc(label):
    """
    Returns an item from the descriptions list, depending on the label (e.g. 3)
    """
    label = int(label)
    return descriptions[label]
