#!/usr/bin/env python

"""Simple, brute-force N-Queens solver."""

__author__ = "collinwinter@google.com (Collin Winter)"


# Pure-Python implementation of itertools.permutations().
def permutations(iterable, r=None):
    """permutations(range(3), 2) --> (0,1) (0,2) (1,0) (1,2) (2,0) (2,1)"""
    pool = tuple(iterable)
    n = len(pool)
    if r is None:
        r = n
    indices = list(range(n))
    cycles = list(range(n-r+1, n+1))[::-1]
    print('indices', indices)
    print('cycles', cycles)
    yield tuple(pool[i] for i in indices[:r])
    while n:
        print('n', n)
        for i in reversed(range(r)):
            print('i', i)
            cycles[i] -= 1
            if cycles[i] == 0:
                indices[i:] = indices[i+1:] + indices[i:i+1]
                cycles[i] = n - i
            else:
                j = cycles[i]
                indices[i], indices[-j] = indices[-j], indices[i]
                yield tuple(pool[i] for i in indices[:r])
                break
        else:
            return


# From http://code.activestate.com/recipes/576647/
def n_queens(queen_count):
    """N-Queens solver.

    Args:
        queen_count: the number of queens to solve for. This is also the
            board size.

    Yields:
        Solutions to the problem. Each yielded value is looks like
        (3, 8, 2, 1, 4, ..., 6) where each number is the column position for the
        queen, and the index into the tuple indicates the row.
    """
    cols = range(queen_count)
    for vec in permutations(cols):
        print('vec', vec, ' cols', cols)
        print('set1', set(vec[i]+i for i in cols))
        print('set2', set(vec[i]-i for i in cols))
        if (queen_count == len(set(vec[i]+i for i in cols))
                        == len(set(vec[i]-i for i in cols))):
            print('yielding ', vec)
            yield vec


if __name__ == "__main__":
    print(list(n_queens(8))[-1])
        