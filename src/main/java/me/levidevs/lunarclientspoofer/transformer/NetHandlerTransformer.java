package me.levidevs.lunarclientspoofer.transformer;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.UUID;

/**
 * @author Levi Taylor
 * @since 04/05/2020
 * Transforms the method `handleJoinGame(S01PacketJoinGame)` in NetHandlerPlayClient to send
 * "vanilla" as the client brand instead of "fml,forge" and registers the Lunar-Client plugin channel.
 */
public class NetHandlerTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // Ignore if this isn't the class we want to transform
        if (!transformedName.equals("net.minecraft.client.network.NetHandlerPlayClient")) {
            return basicClass;
        }

        // Read the class
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        // Goal:
        // Find handleJoinGame(S01PacketJoinGame) [a(hd) when obfuscated]
        // Find ClientBrandRetriever#getClientModName call
        // Replace it with an LDC instruction with "vanilla" as the content

        for (MethodNode method : node.methods) {
            // Real values: handleJoinGame, S01PacketJoinGame
            if (method.name.equals("a") && method.desc.equals("(Lhd;)V")) {
                InsnList instructions = method.instructions;
                Iterator<AbstractInsnNode> iterator = instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode instruction = iterator.next();

                    // Check for correct opcode first and ensure it's a MethodInsnNode
                    if (instruction.getOpcode() == Opcodes.INVOKESTATIC && instruction instanceof MethodInsnNode) {
                        // Instruction has correct opcode
                        MethodInsnNode methodInstruction = (MethodInsnNode) instruction;

                        // Check to see if we have the right method call...
                        if (methodInstruction.owner.equals("net/minecraft/client/ClientBrandRetriever") && methodInstruction.name.equals("getClientModName")) {
                            // Insert LDC instruction and remove this one
                            method.instructions.insertBefore(instruction, new LdcInsnNode("vanilla"));
                            iterator.remove();
                        }
                    } else if (instruction.getOpcode() == Opcodes.RETURN) {
                        // Found the return instruction - remove it so we can add our extra instructions below
                        iterator.remove();
                        break;
                    }
                }

                // Add Lunar Client specific instructions
                InsnList list = new InsnList();

                // Goal: send custom payload with "REGISTER" as type and "Lunar-Client" as value to register channel

                // Byte array instructions for second constructor argument
                InsnList arrayInstructions = new InsnList();
                arrayInstructions.add(new LdcInsnNode("Lunar-Client"));
                arrayInstructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/google/common/base/Charsets", "UTF_8", "Ljava/nio/charset/Charset;"));
                arrayInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B"));

                // Add packet with "REGISTER" as type and instructions above
                addInstructions(list, "REGISTER", arrayInstructions);

                // Send custom payload with "Lunar-Client" as type and a byte array containing the player's UUID
                arrayInstructions.clear();

                arrayInstructions.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "me/levidevs/lunarclientspoofer/transformer/NetHandlerTransformer",
                        "createPacket26",
                        "()[B"
                ));

                addInstructions(list, "Lunar-Client", arrayInstructions);

                // Add the instructions
                method.instructions.add(list);

                // Add return back again since we removed it
                method.instructions.add(new InsnNode(Opcodes.RETURN));

                // Write the node's updated instructions to the writer
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);

                return writer.toByteArray();
            }
        }

        // Return original class in case we some how reach here
        return basicClass;
    }

    /**
     * Adds the instructions required to the specified {@link InsnList} to send a custom payload packet with the
     * specified values.
     * @param list The list to add the instructions to
     * @param type The first value in the custom payload packet
     * @param byteArrayInstructions The instructions that will put a byte array on top of the stack, used as the second
     *                              argument
     */
    private static void addInstructions(InsnList list, String type, InsnList byteArrayInstructions) {
        // Load `this` on to stack
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // Load `netManager` on to stack
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "bjb", "e", "Lej;"));

        // Create new C17PacketCustomPayload
        list.add(new TypeInsnNode(Opcodes.NEW, "iz"));
        list.add(new InsnNode(Opcodes.DUP));

        // First constructor argument
        list.add(new LdcInsnNode(type));

        // Add byte array instructions for second constructor argument
        list.add(byteArrayInstructions);

        // Call constructor
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "iz", "<init>", "(Ljava/lang/String;[B)V"));

        // Create second method parameter - empty array of GenericFutureListener
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "io/netty/util/concurrent/GenericFutureListener"));

        // Send the packet
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ej", "a", "(Lft;[Lio/netty/util/concurrent/GenericFutureListener;)V"));
    }

    /**
     * Gets the bytes required for packet #26, which is used to "authenticate" so the server knowns for certain
     * that the client is Lunar Client.
     * @return An array of 17 bytes, the first byte being the value of 26, and the remaining 16 being the player's UUID
     */
    public static byte[] createPacket26() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[17]);
        buffer.put((byte) 26);

        UUID uuid = Minecraft.getMinecraft().thePlayer.getUniqueID();
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
    }

}
