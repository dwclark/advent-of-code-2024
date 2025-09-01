# Advent of Code 2024

## Overview

## [Day 21](day21.groovy) Objects and State, Then Some Math and "Science"

I think today is a good example of how discovery and insight are achieved in software.

My [initial attempt](day21_part_1.groovy) to solve the problem was very object oriented. I had a hard time understanding what was being asked and what a solution would even look like. In order to understand the problem I kept piling on more and more objects in an attempt to model the state of the system. In other words, I simulated the system as exactly as I could, which is the thing that objects are good at. It was what they were **invented** to do. That was one of the main points of [Simula 67](https://en.wikipedia.org/wiki/Simula), which I think is the world's first language that can be described as object-oriented.

However, direct simulation of a system has its drawbacks. In this case because the objects were tracking every single detail and maintaining every state, the system could not solve part 2. Instead, I had to understand the essence of the system, abstracting only the necessary features necessary to solve the problem. This is more akin to how math and science operate. You forget about certain details and focus on what matters. The "focus on what matters" is in some sense how science fundamentally works. When understanding gravity, you learn that only mass matters, not color, smell, hardness, etc. Once this was done, I was mostly left with functions, with only one object. And that object could easily be replaced with a tuple since it doesn't really do much.

## [Day 22](day22.groovy) Compute Only What You Need!

If you look at the github history for this day you will see that the way I initially solved this problem was:

* Compute **all** price changes + banana prices
* Extract the unique price changes
* Loop over every list of price changes + banana price summing up all of the banana prices for each price change sequence
* Extracting the max total banana price 

This was very costly, but I was able to brute force a solution using threading.

However, threading is almost never the right solution in AofC. If you are reaching for threading, it probably means your algorithm is wrong. In this case, a slight change made all of the difference. Instead of computing **all** of the price changes I did this:

* Compute only the banana price if I had not previously seen a price change sequence in the list and add that to a map since sequence no longer matters
* Sum up all banana prices across all maps computed in previous step
* Extract the max total banana price

Basically this means I eliminated a single loop, but since it was the most expensive loop, it made a huge difference.
