package specman.model.v001;

import specman.editarea.markups.MarkupType;

import java.util.Objects;

public class Markup_V001 {
	final int from, to;
	final MarkupType type;
	
	@Deprecated public Markup_V001() { // For Jackson only
		from = to = 0;
		type = null;
	}

	@Deprecated
	public Markup_V001(int from, int to) {
		this.from = from;
		this.to = to;
		this.type = MarkupType.Changed;
	}

	public Markup_V001(int from, int to, MarkupType type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public MarkupType getType() { return type; }

	public int laenge() {
		return to - from + 1;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Markup_V001 that = (Markup_V001) o;
		return from == that.from && to == that.to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public String toString() {
		return from + ".." + to;
	}
}
