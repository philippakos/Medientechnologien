import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class wave_io {
	public static void main(String[] args) {
		int samples = 0;
		int validBits = 0;
		long sampleRate = 0;
		long numFrames = 0;
		int numChannels = 0;

		String inFilename = null;
		String outFilename = null;

		// String[] files = { "Noise_05", "sine_1k_05" };
		String[] files = { "sine_1k_05" };
		try {
			for (String filestring : files) {

				inFilename = "audio/u5/" + filestring + ".wav";
				outFilename = "audio/u5/" + filestring + "_gain12.wav";
				WavFile readWavFile = WavFile.read_wav(inFilename);

				numFrames = readWavFile.getNumFrames();
				numChannels = readWavFile.getNumChannels();
				samples = (int) numFrames * numChannels;
				validBits = readWavFile.getValidBits();
				sampleRate = readWavFile.getSampleRate();

				float dB = 12;
				float factor = (float) Math.pow(10, dB / 20);

				for (int i = 0; i < samples; i++) {
					float newSound = readWavFile.sound[i];
					newSound *= factor;
					readWavFile.sound[i] = (short) Math.min(Math.max(newSound, Short.MIN_VALUE), Short.MAX_VALUE);
				}

				WavFile.write_wav(outFilename, numChannels, numFrames, validBits, sampleRate, readWavFile.sound);
			}
		} catch (WavFileException | IOException e) {
			System.out.println(e);
		}
	}
}
