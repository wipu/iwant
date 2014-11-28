size(0,100);

// star bottom dimensions
real sbh = 0.4;
real sbw = 0.6;

// star bottom points:
real sbx1 = sbw * 0.1;
real sbx2 = sbw * 0.2;
real sby1 = sbh * 0.2;
real sby2 = sbh * 0.6;

// i bottom width
real ibw = 0.3;

// A N T dimensions
real nh = sbh * 0.5;
real nw = nh;

real x = 10;
real y = 10;
real ix = x-ibw;
real antsep = nw*3;
real ax = x + sbw/2;
real nx = ax + antsep;
real tx = nx + antsep;
real anty = y+sbh+nh/2;
real tw = nh;

guide starBottom(real x, real y) {
  return (x,y+sbh)--(x+sbx2,y+sby2)--(x+sbx1,y)--(x+sbw/2,y+sby1)--(x+sbw-sbx1,y)--(x+sbw-sbx2,y+sby2)--(x+sbw,y+sbh);
}

guide iBottom(real x, real y) {
  return (x-ibw/2,y)--(x+ibw/2,y);
}

guide iAndT(real ix, real iy, real tx, real ty) {
  return (ix,iy)--(ix,iy+sbh)--(ix-ibw,iy+sbh)--(ix-ibw,iy-ibw/2)--(tx+tw/2+ibw/2,iy-ibw/2)--(tx+tw/2+ibw/2,ty+nh)--(tx-tw/2,ty+nh);
}

guide iDot(real x, real y) {
  return (x,y)--(x,y);
}

guide a(real x, real y) {
  return (x-nw/2,y)--(x,y+nh)--(x+nw/2,y);
}

guide n(real x, real y) {
  return (x-nw/2,y)--(x-nw/2,y+nh)--(x+nw/2,y)--(x+nw/2,y+nh);
}

guide tTrunk(real x, real y) {
  return (x,y)--(x,y+nh);
}

real ps = 0.7;
defaultpen(makepen(rotate(45)*xscale(7*ps)*yscale(8*ps)*polygon(32)));

pen starpen = rgb(0.8, 0.8, 0.2);
pen otherpen = rgb(0.3, 0.2, 0.2);

void drawStar() {
  draw(starBottom(x,y), starpen);
  draw(a(ax,anty), starpen);
}

void drawFull() {
  drawStar();
  draw(iBottom(ix,y), otherpen);
  draw(iAndT(ix,y,tx,anty), otherpen);
  draw(iDot(ix,anty+nh/2), otherpen);
  draw(n(nx,anty), otherpen);
  draw(tTrunk(tx,anty), otherpen);
}

drawFull();

shipout(bbox(white,Fill));
