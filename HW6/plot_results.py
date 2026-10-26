"""
plot_results.py

Makes the benchmark plots from results/benchmarks.csv (timings) and results/counts.csv (augmenting paths).
Run from the HW6 folder after `java -jar hw6.jar bench`:   python plot_results.py
"""
import csv
from pathlib import Path

import matplotlib.pyplot as plt

RESULTS = Path(__file__).resolve().parent / "results"
FF, EK = "Ford-Fulkerson (DFS)", "Edmonds-Karp (BFS)"
COLORS = {FF: "#eb6834", EK: "#2a78d6"}
MARKERS = {FF: "o", EK: "s"}
FAMILIES = [
    ("sparse", "Sparse random (E = 10V, cap <= 100)"),
    ("dense", "Dense random (E = V^2/2, cap <= 100)"),
    ("adversarial", "Adversarial (chain-first, cap <= 10^6)"),
]


def style(ax, title, ylabel, sizes):
    ax.set_title(title, fontsize=10, loc="left")
    ax.set_xscale("log")
    ax.set_yscale("log")
    # label the x axis with the sizes we actually ran, not 3x10^1 style ticks
    ax.set_xticks(sizes)
    ax.set_xticklabels([str(v) for v in sizes])
    ax.minorticks_off()
    ax.set_xlabel("vertices V")
    ax.set_ylabel(ylabel)
    ax.grid(True, color="#e4e3dc", linewidth=0.8)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)


def line(ax, xs, ys, name):
    ax.plot(xs, ys, color=COLORS[name], marker=MARKERS[name], markersize=6, linewidth=2, label=name)
    # label the end of each line so it doesn't depend on color alone
    ax.annotate(name.split(" ")[0], (xs[-1], ys[-1]), xytext=(5, 0), textcoords="offset points",
                fontsize=8, va="center", color="#3a3a38")


def plot_runtime():
    rows = list(csv.DictReader(open(RESULTS / "benchmarks.csv", newline="")))
    fig, axes = plt.subplots(1, 3, figsize=(15, 4.3))
    for ax, (family, title) in zip(axes, FAMILIES):
        for name in (FF, EK):
            pts = sorted((int(r["V"]), float(r["median_ms"])) for r in rows if r["family"] == family and r["algorithm"] == name)
            line(ax, [p[0] for p in pts], [p[1] for p in pts], name)
        style(ax, title, "median ms per max-flow run", [p[0] for p in pts])
    axes[0].legend(frameon=False, fontsize=8)
    fig.suptitle("Runtime vs graph size", x=0.01, ha="left", fontsize=12)
    fig.tight_layout()
    fig.savefig(RESULTS / "runtime.png", dpi=150)
    print("wrote results/runtime.png")


def plot_augmentations():
    rows = list(csv.DictReader(open(RESULTS / "counts.csv", newline="")))
    fig, axes = plt.subplots(1, 3, figsize=(15, 4.3))
    for ax, (family, title) in zip(axes, FAMILIES):
        fam = [r for r in rows if r["family"] == family]
        v = [int(r["V"]) for r in fam]
        line(ax, v, [int(r["ff_augmentations"]) for r in fam], FF)
        line(ax, v, [int(r["ek_augmentations"]) for r in fam], EK)
        ax.plot(v, [int(r["ek_bound_VE_over_2"]) for r in fam], color="#8a8980", linestyle="--", linewidth=1.5,
                label="Edmonds-Karp bound V*E/2")
        style(ax, title, "augmenting paths", v)
    axes[0].legend(frameon=False, fontsize=8)
    fig.suptitle("Augmenting paths vs graph size", x=0.01, ha="left", fontsize=12)
    fig.tight_layout()
    fig.savefig(RESULTS / "augmentations.png", dpi=150)
    print("wrote results/augmentations.png")


if __name__ == "__main__":
    plot_runtime()
    plot_augmentations()
