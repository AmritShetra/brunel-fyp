labels = [
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7"
]

one = """The item can be put in the recycling bin.
Just make sure to empty and rinse it first (if necessary)."""
two = """The item can be put in the recycling bin. Although...
Flimsy plastics (such as carrier bags) can be recycled in the supermarket."""
three = """Not recyclable in normal conditions.
Check any plastic lumber makers or local waste management."""
four = """These items can be recycled in some councils...
Plastic bags can be recycled in local supermarkets."""
five = """The item can be put in the recycle bin.
Just throw away any loose caps in the rubbish bin."""
six = """Not recyclable in normal conditions, throw it away."""
seven = """Not recyclable, probably best to throw it away."""

descriptions = {
    1: one,
    2: two,
    3: three,
    4: four,
    5: five,
    6: six,
    7: seven
}


def get_desc(label):
    """
    Returns an item from the descriptions list, depending on the label (e.g. 3)
    """
    label = int(label)
    return descriptions[label]
