package haven;

import javax.media.opengl.GL2;
import java.nio.FloatBuffer;

import static haven.MCache.cutsz;
import static haven.MCache.tilesz;

public class GridOverlay extends MapOverlay {
    private final FloatBuffer[] vertexBuffers;
    private final int area;
    private final Coord size;
    private final States.ColState color;
    private Location location;
    private Coord ul;
    private int curIndex;

    public GridOverlay(MCache map, Coord size) {
        super(map);
        this.size = size;
        this.area = (size.x + 1) * (size.y + 1);
        this.color = new States.ColState(255, 36, 0, 128);

        // double-buffer to prevent flickering
        vertexBuffers = new FloatBuffer[2];
        for (int i = 0; i < vertexBuffers.length; i++)
            vertexBuffers[i] = Utils.mkfbuf(this.area * 3 * 4);
        curIndex = 0;
    }

    @Override
    public void draw(GOut g) {
        g.apply();
        BGL gl = g.gl;
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL2.GL_FLOAT, 0, Utils.bufcp(getCurrentBuffer()));
        gl.glLineWidth(1.0F);
        gl.glDrawArrays(GL2.GL_LINES, 0, area * 4);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }

    @Override
    public boolean setup(RenderList rl) {
        rl.prepo(location);
        rl.prepo(color);
        return true;
    }

    @Override
    protected void refresh(Coord cut) {
        this.ul = cut.sub(MapView.view, MapView.view).mul(cutsz);
        this.location = Location.xlate(new Coord3f((float)(ul.x * tilesz.x), (float)(-ul.y * tilesz.y), 0.0F));
        Coord c = new Coord();
        FloatBuffer vbuf = getBackBuffer();
        vbuf.rewind();
        for (c.y = ul.y; c.y < ul.y + size.y; c.y++)
            for (c.x = ul.x; c.x < ul.x + size.x; c.x++)
               addLineStrip(vbuf, mapToScreen(c), mapToScreen(c.add(1, 0)), mapToScreen(c.add(1, 1)));
        swapBuffers();
    }

    private Coord3f mapToScreen(Coord c) {
        return new Coord3f((float)((c.x - ul.x) * tilesz.x), (float)(-(c.y - ul.y) * tilesz.y), map.getz(c));
    }

    private void addLineStrip(FloatBuffer vbuf, Coord3f... vertices) {
        for (int i = 0; i < vertices.length - 1; i++) {
            Coord3f a = vertices[i];
            Coord3f b = vertices[i + 1];
            vbuf.put(a.x).put(a.y).put(a.z + 0.1f);
            vbuf.put(b.x).put(b.y).put(b.z + 0.1f);
        }
    }

    private FloatBuffer getCurrentBuffer() {
        return vertexBuffers[curIndex];
    }

    private FloatBuffer getBackBuffer() {
        return vertexBuffers[(curIndex + 1) % 2];
    }

    private void swapBuffers() {
        curIndex = (curIndex + 1) % 2;
        getCurrentBuffer().rewind();
    }
}
