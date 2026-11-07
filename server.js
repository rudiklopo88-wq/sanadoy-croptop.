import express from "express";
import cors from "cors";
import multer from "multer";
import OpenAI from "openai";
import dotenv from "dotenv";
dotenv.config();
const app=express();
app.use(cors());
const upload=multer({storage:multer.memoryStorage(),limits:{fileSize:10*1024*1024}});
const client=new OpenAI({apiKey:process.env.OPENAI_API_KEY});
app.get("/health",(req,res)=>res.json({ok:true,app:"Sanadoy croptop"}));
app.post("/solve",upload.single("image"),async(req,res)=>{
  try{
    if(!process.env.OPENAI_API_KEY) return res.status(500).json({error:"OPENAI_API_KEY belum diatur"});
    if(!req.file) return res.status(400).json({error:"Gambar soal tidak ditemukan"});
    const data=req.file.buffer.toString("base64");
    const response=await client.chat.completions.create({
      model:"gpt-4o-mini",
      messages:[{role:"user",content:[
        {type:"text",text:"Bacalah soal pada gambar. Jawab dalam Bahasa Indonesia. Tulis: (1) jawaban akhir, (2) langkah penjelasan yang runtut, (3) jika pilihan ganda, sebutkan huruf dan alasan. Jika gambar tidak terbaca, jelaskan bagian yang perlu diperjelas."},
        {type:"image_url",image_url:{url:`data:${req.file.mimetype};base64,${data}`}}
      ]}],
      max_tokens:1200
    });
    res.json({answer:response.choices?.[0]?.message?.content || "AI tidak memberikan jawaban."});
  }catch(e){console.error(e);res.status(500).json({error:"Gagal memproses soal",detail:e.message});}
});
const port=process.env.PORT||3000;
app.listen(port,()=>console.log(`Sanadoy croptop backend aktif di port ${port}`));
