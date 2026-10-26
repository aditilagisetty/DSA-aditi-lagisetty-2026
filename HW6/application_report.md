# Application Report: Matching Students to Project Teams

## The problem

There are 60 students and 12 project teams with 5 spots each (60 spots total). Each student picks 3 teams. The goal is to put as many students as possible on a team they picked, without putting more than 5 people on any team.

## Turning it into a max flow problem

`TeamMatcher` in `TeamMatching.kt` builds this network:

```
source ──1──▶ student ──1──▶ each team the student picked ──5──▶ sink
```

- source → each student, capacity 1, so a student can only be placed once.
- student → each team they picked, capacity 1. There are only edges to teams the student picked.
- each team → sink, capacity 5, so a team can't go over its spots.

All the capacities are whole numbers, so the algorithms only send whole numbers of flow. Each student → team edge ends up carrying 0 or 1, and a 1 means that student goes on that team. This is always a valid assignment: nobody is on two teams, no team goes over 5, and everyone is on a team they picked. Any valid assignment can also be turned into a flow of the same size, so the max flow is the most students we can place.

## The made-up class

`SyntheticClass.generate()` makes up each student's 3 picks at random. It makes some teams more popular than others, like a real class: team number k is picked with weight 1/(k+1)^1.5. It uses a fixed seed, so it's the same class every time. AI Tutor was picked by 53 students and Game Engine by 39, while Fitness Tracker and Library Catalog were picked by only 4 and 3.

## Results

The max flow is 56. So 56 of the 60 students get a team they picked, and no assignment could do better. 20 got their 1st choice, 16 their 2nd and 20 their 3rd. Every team is full except Fitness Tracker (4/5) and Library Catalog (2/5). Both algorithms get 56, and the tests check the assignment is valid.

These 4 students didn't get placed:

| Student | Picked |
|---|---|
| Student 54 | Game Engine, AI Tutor, Budget App |
| Student 55 | AI Tutor, Campus Map, Game Engine |
| Student 56 | Game Engine, Chess Bot, AI Tutor |
| Student 57 | Game Engine, Campus Map, AI Tutor |

## Why they couldn't be placed (the min cut)

There are still 4 empty spots, so the reason isn't that the class ran out of room. The min cut shows the real reason.

After the max flow, I find every vertex that can still be reached from the source in the residual graph (`FlowNetwork.minCutSourceSide`). That group has:

- 8 teams: AI Tutor, Game Engine, Campus Map, Music Recommender, Budget App, Chess Bot, Recipe Planner and Compiler, with 8 × 5 = 40 spots;
- 44 students, who all picked only teams from those 8.

So 44 students are competing for 40 spots, and none of them would take any other team. At least 44 − 40 = 4 of them can't be placed, however you assign people. That's exactly how many were left out, which proves 56 is the best possible. It also matches the theorem: the edges crossing the cut are the 8 full team → sink edges (40) plus the source → student edges of the 16 students outside the group (16), and 40 + 16 = 56.

In plain terms, a team is in the group if an unplaced student could get onto it by bumping someone to another team they picked, who bumps someone else, and so on. Every team reachable that way is already full. Nobody on them can move out, because they also only picked teams in the group. The empty spots can't be reached, because none of these 44 students picked Fitness Tracker or Library Catalog. This is the same idea as Hall's theorem for matchings.

`HW6Test.kt` checks this on 50 random classes. Each time, the unplaced students are in the group, the group's students only picked the group's teams, those teams are full, and (students in group) − (spots in group) = number unplaced.

Max flow tells you how many students can be placed, but not which ones miss out. Any 4 of the 44 could have been left out. Here it was students 54 to 57 just because they come last in the input. Shuffling the students first would make it fairer.

## What max flow doesn't handle

Max flow treats all 3 choices as equally good, so 20 people got their 3rd choice. To prefer first choices, you could give each student → team edge a cost (1st = 1, 2nd = 2, 3rd = 3) and use min-cost max flow. That would still place 56 students, but with as few low-ranked placements as possible.
