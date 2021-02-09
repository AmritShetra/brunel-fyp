labels = [
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7"
]

one = """Can be put in the recycle bin, as long as it is emptied and rinsed."""
two = """Can be put in the recycle bin, but flimsy plastics (e.g. carrier bags)
        can be recycled in your local supermarket."""
three = """Not recyclable in normal conditions. Check any plastic lumber makers
        or local waste management."""
four = """Can be recycled in some councils. Plastic bags can be recycled in
        local supermarkets."""
five = """Can be put in the recycle bin, but loose caps can thrown away in the
        rubbish."""
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
