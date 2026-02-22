package com.Hoolheyak.spine38;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ShortArray;

import com.Hoolheyak.spine38.attachments.Attachment;
import com.Hoolheyak.spine38.attachments.ClippingAttachment;
import com.Hoolheyak.spine38.attachments.MeshAttachment;
import com.Hoolheyak.spine38.attachments.RegionAttachment;
import com.Hoolheyak.spine38.attachments.SkeletonAttachment;
import com.Hoolheyak.spine38.utils.SkeletonClipping;

public class SkeletonRenderer {
	static private final short[] quadTriangles = {0, 1, 2, 2, 3, 0};

	private boolean premultipliedAlpha;
	private final FloatArray vertices = new FloatArray(32);
	private final SkeletonClipping clipper = new SkeletonClipping();
	private VertexEffect vertexEffect;
	private final Vector2 temp = new Vector2();
	private final Vector2 temp2 = new Vector2();
	private final Color temp3 = new Color();
	private final Color temp4 = new Color();
	private final Color temp5 = new Color();
	private final Color temp6 = new Color();

	/** * 修改说明：移除了对 TwoColorPolygonBatch 的引用。
	 * 现在只支持普通 Batch 和 PolygonSpriteBatch。
	 */
	public void draw (Batch batch, Skeleton skeleton) {
		// 【删除】移除了 TwoColorPolygonBatch 的判断分支

		if (batch instanceof PolygonSpriteBatch) {
			draw((PolygonSpriteBatch)batch, skeleton);
			return;
		}
		if (batch == null) throw new IllegalArgumentException("batch cannot be null.");
		if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");

		VertexEffect vertexEffect = this.vertexEffect;
		if (vertexEffect != null) vertexEffect.begin(skeleton);

		boolean premultipliedAlpha = this.premultipliedAlpha;
		BlendMode blendMode = null;
		float[] vertices = this.vertices.items;
		Color skeletonColor = skeleton.color;
		float r = skeletonColor.r, g = skeletonColor.g, b = skeletonColor.b, a = skeletonColor.a;
		Array<Slot> drawOrder = skeleton.drawOrder;
		for (int i = 0, n = drawOrder.size; i < n; i++) {
			Slot slot = drawOrder.get(i);
			if (!slot.bone.active) {
				clipper.clipEnd(slot);
				continue;
			}
			Attachment attachment = slot.attachment;
			if (attachment instanceof RegionAttachment) {
				RegionAttachment region = (RegionAttachment)attachment;
				region.computeWorldVertices(slot.getBone(), vertices, 0, 5);
				Color color = region.getColor(), slotColor = slot.getColor();
				float alpha = a * slotColor.a * color.a * 255;
				float multiplier = premultipliedAlpha ? alpha : 255;

				BlendMode slotBlendMode = slot.data.getBlendMode();
				if (slotBlendMode != blendMode) {
					if (slotBlendMode == BlendMode.additive && premultipliedAlpha) {
						slotBlendMode = BlendMode.normal;
						alpha = 0;
					}
					blendMode = slotBlendMode;
					batch.setBlendFunction(blendMode.getSource(premultipliedAlpha), blendMode.getDest());
				}

				float c = NumberUtils.intToFloatColor(((int)alpha << 24) //
						| ((int)(b * slotColor.b * color.b * multiplier) << 16) //
						| ((int)(g * slotColor.g * color.g * multiplier) << 8) //
						| (int)(r * slotColor.r * color.r * multiplier));
				float[] uvs = region.getUVs();
				for (int u = 0, v = 2; u < 8; u += 2, v += 5) {
					vertices[v] = c;
					vertices[v + 1] = uvs[u];
					vertices[v + 2] = uvs[u + 1];
				}

				if (vertexEffect != null) applyVertexEffect(vertices, 20, 5, c, 0);

				batch.draw(region.getRegion().getTexture(), vertices, 0, 20);

			} else if (attachment instanceof ClippingAttachment) {
				clipper.clipStart(slot, (ClippingAttachment)attachment);
				continue;

			} else if (attachment instanceof MeshAttachment) {
				// 【修改】报错信息里去掉了 TwoColorPolygonBatch
				throw new RuntimeException(batch.getClass().getSimpleName()
						+ " cannot render meshes, PolygonSpriteBatch is required.");

			} else if (attachment instanceof SkeletonAttachment) {
				Skeleton attachmentSkeleton = ((SkeletonAttachment)attachment).getSkeleton();
				if (attachmentSkeleton != null) draw(batch, attachmentSkeleton);
			}

			clipper.clipEnd(slot);
		}
		clipper.clipEnd();
		if (vertexEffect != null) vertexEffect.end();
	}

	@SuppressWarnings("null")
	public void draw (PolygonSpriteBatch batch, Skeleton skeleton) {
		if (batch == null) throw new IllegalArgumentException("batch cannot be null.");
		if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");

		Vector2 tempPosition = this.temp, tempUV = this.temp2;
		Color tempLight1 = this.temp3, tempDark1 = this.temp4;
		Color tempLight2 = this.temp5, tempDark2 = this.temp6;
		VertexEffect vertexEffect = this.vertexEffect;
		if (vertexEffect != null) vertexEffect.begin(skeleton);

		boolean premultipliedAlpha = this.premultipliedAlpha;
		BlendMode blendMode = null;
		int verticesLength = 0;
		float[] vertices = null, uvs = null;
		short[] triangles = null;
		Color color = null, skeletonColor = skeleton.color;
		float r = skeletonColor.r, g = skeletonColor.g, b = skeletonColor.b, a = skeletonColor.a;
		Array<Slot> drawOrder = skeleton.drawOrder;
		for (int i = 0, n = drawOrder.size; i < n; i++) {
			Slot slot = drawOrder.get(i);
			if (!slot.bone.active) {
				clipper.clipEnd(slot);
				continue;
			}
			Texture texture = null;
			int vertexSize = clipper.isClipping() ? 2 : 5;
			Attachment attachment = slot.attachment;
			if (attachment instanceof RegionAttachment) {
				RegionAttachment region = (RegionAttachment)attachment;
				verticesLength = vertexSize << 2;
				vertices = this.vertices.items;
				region.computeWorldVertices(slot.getBone(), vertices, 0, vertexSize);
				triangles = quadTriangles;
				texture = region.getRegion().getTexture();
				uvs = region.getUVs();
				color = region.getColor();

			} else if (attachment instanceof MeshAttachment) {
				MeshAttachment mesh = (MeshAttachment)attachment;
				int count = mesh.getWorldVerticesLength();
				verticesLength = (count >> 1) * vertexSize;
				vertices = this.vertices.setSize(verticesLength);
				mesh.computeWorldVertices(slot, 0, count, vertices, 0, vertexSize);
				triangles = mesh.getTriangles();
				texture = mesh.getRegion().getTexture();
				uvs = mesh.getUVs();
				color = mesh.getColor();

			} else if (attachment instanceof ClippingAttachment) {
				ClippingAttachment clip = (ClippingAttachment)attachment;
				clipper.clipStart(slot, clip);
				continue;

			} else if (attachment instanceof SkeletonAttachment) {
				Skeleton attachmentSkeleton = ((SkeletonAttachment)attachment).getSkeleton();
				if (attachmentSkeleton != null) draw(batch, attachmentSkeleton);
			}

			if (texture != null) {
				Color slotColor = slot.getColor();
				float alpha = a * slotColor.a * color.a * 255;
				float multiplier = premultipliedAlpha ? alpha : 255;

				BlendMode slotBlendMode = slot.data.getBlendMode();
				if (slotBlendMode != blendMode) {
					if (slotBlendMode == BlendMode.additive && premultipliedAlpha) {
						slotBlendMode = BlendMode.normal;
						alpha = 0;
					}
					blendMode = slotBlendMode;
					batch.setBlendFunction(blendMode.getSource(premultipliedAlpha), blendMode.getDest());
				}

				float c = NumberUtils.intToFloatColor(((int)alpha << 24) //
						| ((int)(b * slotColor.b * color.b * multiplier) << 16) //
						| ((int)(g * slotColor.g * color.g * multiplier) << 8) //
						| (int)(r * slotColor.r * color.r * multiplier));

				if (clipper.isClipping()) {
					clipper.clipTriangles(vertices, verticesLength, triangles, triangles.length, uvs, c, 0, false);
					FloatArray clippedVertices = clipper.getClippedVertices();
					ShortArray clippedTriangles = clipper.getClippedTriangles();
					if (vertexEffect != null) applyVertexEffect(clippedVertices.items, clippedVertices.size, 5, c, 0);
					batch.draw(texture, clippedVertices.items, 0, clippedVertices.size, clippedTriangles.items, 0,
							clippedTriangles.size);
				} else {
					if (vertexEffect != null) {
						tempLight1.set(NumberUtils.floatToIntColor(c));
						tempDark1.set(0);
						for (int v = 0, u = 0; v < verticesLength; v += 5, u += 2) {
							tempPosition.x = vertices[v];
							tempPosition.y = vertices[v + 1];
							tempLight2.set(tempLight1);
							tempDark2.set(tempDark1);
							tempUV.x = uvs[u];
							tempUV.y = uvs[u + 1];
							vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
							vertices[v] = tempPosition.x;
							vertices[v + 1] = tempPosition.y;
							vertices[v + 2] = tempLight2.toFloatBits();
							vertices[v + 3] = tempUV.x;
							vertices[v + 4] = tempUV.y;
						}
					} else {
						for (int v = 2, u = 0; v < verticesLength; v += 5, u += 2) {
							vertices[v] = c;
							vertices[v + 1] = uvs[u];
							vertices[v + 2] = uvs[u + 1];
						}
					}
					batch.draw(texture, vertices, 0, verticesLength, triangles, 0, triangles.length);
				}
			}

			clipper.clipEnd(slot);
		}
		clipper.clipEnd();
		if (vertexEffect != null) vertexEffect.end();
	}

	// 【删除】移除了 public void draw (TwoColorPolygonBatch batch, Skeleton skeleton) 整个方法

	private void applyVertexEffect (float[] vertices, int verticesLength, int stride, float light, float dark) {
		Vector2 tempPosition = this.temp, tempUV = this.temp2;
		Color tempLight1 = this.temp3, tempDark1 = this.temp4;
		Color tempLight2 = this.temp5, tempDark2 = this.temp6;
		VertexEffect vertexEffect = this.vertexEffect;
		tempLight1.set(NumberUtils.floatToIntColor(light));
		tempDark1.set(NumberUtils.floatToIntColor(dark));
		if (stride == 5) {
			for (int v = 0; v < verticesLength; v += stride) {
				tempPosition.x = vertices[v];
				tempPosition.y = vertices[v + 1];
				tempUV.x = vertices[v + 3];
				tempUV.y = vertices[v + 4];
				tempLight2.set(tempLight1);
				tempDark2.set(tempDark1);
				vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
				vertices[v] = tempPosition.x;
				vertices[v + 1] = tempPosition.y;
				vertices[v + 2] = tempLight2.toFloatBits();
				vertices[v + 3] = tempUV.x;
				vertices[v + 4] = tempUV.y;
			}
		} else {
			for (int v = 0; v < verticesLength; v += stride) {
				tempPosition.x = vertices[v];
				tempPosition.y = vertices[v + 1];
				tempUV.x = vertices[v + 4];
				tempUV.y = vertices[v + 5];
				tempLight2.set(tempLight1);
				tempDark2.set(tempDark1);
				vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
				vertices[v] = tempPosition.x;
				vertices[v + 1] = tempPosition.y;
				vertices[v + 2] = tempLight2.toFloatBits();
				vertices[v + 3] = tempDark2.toFloatBits();
				vertices[v + 4] = tempUV.x;
				vertices[v + 5] = tempUV.y;
			}
		}
	}

	public boolean getPremultipliedAlpha () {
		return premultipliedAlpha;
	}

	public void setPremultipliedAlpha (boolean premultipliedAlpha) {
		this.premultipliedAlpha = premultipliedAlpha;
	}

	/** @return May be null. */
	public VertexEffect getVertexEffect () {
		return vertexEffect;
	}

	/** @param vertexEffect May be null. */
	public void setVertexEffect (VertexEffect vertexEffect) {
		this.vertexEffect = vertexEffect;
	}

	/** Modifies the skeleton or vertex positions, UVs, or colors during rendering. */
	static public interface VertexEffect {
		public void begin (Skeleton skeleton);

		public void transform (Vector2 position, Vector2 uv, Color color, Color darkColor);

		public void end ();
	}
}