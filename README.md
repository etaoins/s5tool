# Static Site S3 Tool
s5tool is a tool for uploading a static site to an Amazon S3 bucket. It's similar to [s3cmd sync](http://s3tools.org/s3cmd) but focused on static site hosting instead of generic file storage

s5tool will upload any differences between a local directory and an S3 bucket to the bucket. Any extra files on the bucket are deleted; for this reason it's not possible to store additional files in an s5tool bucket.

This is intended to be used as an uploader for a static site generator such as [Jekyll](http://jekyllrb.com) or [Hyde](http://ringce.com/hyde)

## Usage

1. Install Java 7 and [sbt](http://www.scala-sbt.org)
2. Create a ~/.s3cfg file using `s3cmd --configure` or by manually specifying your S3 keys:

		[default]
		access_key = ACCESSKEYGOESHERE
		secret_key = SECRETKEYGOESHERE

3. Run s5tool from the source root as `sbt path/to/site/root bucketname` For example:

		sbt run ~/sites/tech-blog www.tech-blog.org

	To display additional options run `sbt run --help`

## Opportunistic gzip Compression 	

S3 does not support negotiating gzip compression with a web browser. To serve compressed assets the S3 object must be uploaded in compressed form.

To work around this behavior s5tool will try to gzip compress every file it encounters. If the compressed version is smaller than the uncompressed version the compressed version will be uploaded to S3. This is preferable for static sites but has a few drawbacks:

- Compression can be computationally intensive. s5tool uses a ForkJoinPool to distribute the load across all available cores which greatly speeds up most workloads.
- The gzipped files are kept in memory before uploading. It might be necessary to adjust Java's heap size for larger sites
- Some user agents do not support gzip encoding. s5tool is only recommended for user-facing sites accessed by typical mobile and desktop browsers. Files that need to be accessed by non-browser user agents should be uploaded by another means.
